/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.integration.gateway.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;

/**
 * 网关配置类 —— Sentinel 网关限流 + CORS 跨域
 *
 * <p>本类是"Sentinel 整合 Spring Cloud Gateway"的标准三件套写法：</p>
 * <ol>
 *   <li>{@link #sentinelGatewayFilter()}：注册 Sentinel 全局过滤器（入口）；</li>
 *   <li>{@link #sentinelGatewayBlockExceptionHandler()}：注册被限流后的异常处理器（出口）；</li>
 *   <li>{@link #initGatewayRules()}：通过代码加载网关限流规则。</li>
 * </ol>
 *
 * <p>限流目标：路由 ID 为 "praiseItemSentinel" 的点赞接口
 * （对应 Nacos 中 integrated-gateway.yaml 的 {@code Path=/praise/sentinel} 路由）。</p>
 *
 * @author TrevorLink
 */
@Configuration
public class GatewayConfig {

	/** 视图解析器列表：用于限流异常时按 WebFlux 规范渲染错误响应 */
	private final List<ViewResolver> viewResolvers;

	/** HTTP 编解码配置：读取请求体/编码响应体时使用 */
	private final ServerCodecConfigurer serverCodecConfigurer;

	/**
	 * 构造器注入：ObjectProvider 做懒加载/可选注入，
	 * 容器中没有 ViewResolver 时给空集合兜底，避免启动失败。
	 */
	public GatewayConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
			ServerCodecConfigurer serverCodecConfigurer) {
		this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
		this.serverCodecConfigurer = serverCodecConfigurer;
	}

	/**
	 * 【三件套之一】Sentinel 网关全局过滤器。
	 *
	 * <p>所有经过网关的请求都会先过这个过滤器，Sentinel 在这里统计 QPS 并判断
	 * 是否触发限流规则；触发则抛出 BlockException，交给下面的异常处理器返回兜底响应。</p>
	 *
	 * <p>@Order(HIGHEST_PRECEDENCE)：把优先级设为最高，保证在业务路由过滤器之前执行。</p>
	 */
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public GlobalFilter sentinelGatewayFilter() {
		return new SentinelGatewayFilter();
	}

	/**
	 * 【三件套之二】代码方式加载网关流控规则。
	 *
	 * <p>规则含义：对路由 "praiseItemSentinel"（即 /praise/sentinel 点赞接口）
	 * 限流 —— 统计窗口 1 秒（setIntervalSec(1)），最多放过 5 个请求（setCount(5)），
	 * 超出的请求被 Block。</p>
	 *
	 * <p>生产环境中规则通常存到 Nacos/Apollo 由控制台推送，实现动态限流；
	 * 本示例为简化演示，采用 @PostConstruct 启动时写死加载。</p>
	 */
	@PostConstruct
	public void initGatewayRules() {
		Set<GatewayFlowRule> rules = new HashSet<>();
		rules.add(
				// 资源名 = 网关路由 ID；1 秒内最多 5 次，超出即限流
				new GatewayFlowRule("praiseItemSentinel").setCount(5).setIntervalSec(1));
		GatewayRuleManager.loadRules(rules);
	}

	/**
	 * 【三件套之三】Sentinel 限流异常处理器。
	 *
	 * <p>请求被限流时 Sentinel 会抛出 BlockException，
	 * 该 Handler 负责把它转换成友好的 HTTP 响应，而不是抛 500。</p>
	 */
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
		// 为 Spring Cloud Gateway 注册限流异常处理器。
		return new SentinelGatewayBlockExceptionHandler(viewResolvers,
				serverCodecConfigurer);
	}

	/**
	 * 自定义限流兜底响应内容。
	 *
	 * <p>默认被限流返回 429 + 简单文本；这里改为返回 HTTP 200 +
	 * JSON 文案 "此接口被限流了"，方便前端页面直接展示。</p>
	 *
	 * <p>注意：返回的是 Reactor 的 Mono（WebFlux 响应式编程模型，异步单值），
	 * 这是 Gateway 体系与普通 MVC 最大的编码差异。</p>
	 */
	@PostConstruct
	public void initBlockHandlers() {
		BlockRequestHandler blockRequestHandler = new BlockRequestHandler() {
			@Override
			public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange,
					Throwable throwable) {
				return ServerResponse.status(HttpStatus.OK)
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.body(BodyInserters.fromObject("此接口被限流了"));
			}
		};
		GatewayCallbackManager.setBlockHandler(blockRequestHandler);
	}

	/**
	 * CORS 跨域过滤器。
	 *
	 * <p>网关作为统一入口，跨域在网关层一次性解决，后端微服务无需关心 CORS：
	 * 允许所有来源（OriginPattern *）、所有请求头、所有 HTTP 方法，并允许携带 Cookie。</p>
	 *
	 * <p>提示：allowCredentials=true 与 addAllowedOrigin("*") 在较新版本中不兼容，
	 * 因此这里用的是 addAllowedOriginPattern("*")，这是标准解法。</p>
	 */
	@Bean
	public CorsWebFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.addAllowedOriginPattern("*");
		// 对所有路径 /** 生效
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}

}
