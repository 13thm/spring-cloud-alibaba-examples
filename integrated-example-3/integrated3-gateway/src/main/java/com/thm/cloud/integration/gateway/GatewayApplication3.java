package com.thm.cloud.integration.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类（端口 30020）。
 *
 * <p>整个整合示例的统一流量入口，核心职责：</p>
 * <ol>
 *   <li>作为 Spring Cloud Gateway，把请求按路由规则转发到后端微服务；</li>
 *   <li>路由规则不在本地写死，而是从 Nacos 配置中心动态加载
 *       （见 application.yaml 中 spring.config.import 引入 integrated2-gateway.yaml），
 *       修改 Nacos 上的路由配置并发布即可实时生效，无需重启网关；</li>
 *   <li>整合 Sentinel，对指定路由（点赞接口）做网关级限流，
 *       超过阈值的请求直接返回"此接口被限流了"。</li>
 * </ol>
 *
 * <p>注意：Gateway 基于 WebFlux（Reactor 响应式栈），不能引入 spring-boot-starter-web。</p>
 *
 * @author thm
 */
@SpringBootApplication
// 显式开启服务发现客户端（新版本 Spring Cloud 中该注解可省略，此处保留以示语义：
// 网关需要从 Nacos 拉取服务列表，才能用 lb://服务名 做负载均衡转发）
@EnableDiscoveryClient
public class GatewayApplication3 {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication3.class, args);
	}

}
