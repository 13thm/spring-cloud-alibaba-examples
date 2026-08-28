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

package com.alibaba.cloud.integration.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 页面路由控制器 —— 只做"URL → Thymeleaf 模板名"的映射。
 *
 * <p>注意与 @RestController 的区别：这里返回的字符串是
 * templates/ 目录下的模板文件名（不含 .html 后缀），
 * 由 Thymeleaf 渲染成 HTML 返回浏览器，而不是 JSON。</p>
 *
 * <p>三个页面与演示场景一一对应：</p>
 * <ul>
 *   <li>/order —— 下单页面：演示 Seata 分布式事务（含事务回滚按钮）；</li>
 *   <li>/rocketmq —— 点赞页面：演示 RocketMQ 削峰填谷（异步消费，点赞数延迟增长）；</li>
 *   <li>/sentinel —— 限流页面：演示 Sentinel 网关限流（快速点击会被拦截）。</li>
 * </ul>
 *
 * @author HuangSir
 * @date 2022-09-08 14:00
 */
@Controller
public class IntegrationController {

	/**
	 * 渲染 templates/order.html —— 下单/分布式事务演示页。
	 */
	@RequestMapping("/order")
	public String order() {
		return "order";
	}

	/**
	 * 渲染 templates/rocketmq.html —— RocketMQ 削峰填谷演示页。
	 */
	@RequestMapping("/rocketmq")
	public String rocketmq() {
		return "rocketmq";
	}

	/**
	 * 渲染 templates/sentinel.html —— Sentinel 网关限流演示页。
	 */
	@RequestMapping("/sentinel")
	public String sentinel() {
		return "sentinel";
	}

}
