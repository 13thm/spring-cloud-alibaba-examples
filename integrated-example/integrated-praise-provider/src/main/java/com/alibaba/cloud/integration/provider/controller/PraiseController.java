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

package com.alibaba.cloud.integration.provider.controller;

import com.alibaba.cloud.integration.provider.message.PraiseMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 点赞接口 —— RocketMQ 消息生产者。
 *
 * <p>一个接口服务两个演示场景（两个路径都打到同一逻辑）：</p>
 * <ul>
 *   <li>/praise/sentinel —— 配合网关 Sentinel 限流演示：QPS 超过 5 时，
 *       请求在网关就被拦截返回"此接口被限流了"，根本到不了这里；</li>
 *   <li>/praise/rocketmq —— 配合 RocketMQ 削峰填谷演示：无论瞬时流量多大，
 *       这里只做"把消息扔进 MQ"这一轻量动作，数据库压力由消费者端控制。</li>
 * </ul>
 *
 * <p>两种方案对比：限流是"直接拒绝多余流量"（用户感知到被限），
 * MQ 削峰是"先收下来慢慢处理"（用户无感知，赞数延迟到达）。</p>
 *
 * @author TrevorLink
 */
@RestController
@RequestMapping("/praise")
public class PraiseController {

	/** 输出绑定名：对应 Nacos 配置 integrated-provider.yaml 中 bindings.praise-output */
	private static final String BINDING_NAME = "praise-output";

	/**
	 * StreamBridge：Spring Cloud Stream 函数式模型下的"动态发消息工具"。
	 * 相比老版本的 Source 接口 + @EnableBinding，无需预定义输出通道，
	 * 指定绑定名即可发送，是新版本官方推荐用法。
	 */
	@Autowired
	private StreamBridge streamBridge;

	/**
	 * 点赞接口：把点赞消息发送到 RocketMQ。
	 *
	 * <p>流程：构造业务消息 → MessageBuilder 包装为 Spring Messaging 消息
	 * → StreamBridge 按绑定名 praise-output 发送到 topic PRAISE-TOPIC-01。
	 * 发送即返回（异步解耦），写库工作完全交给消费者。</p>
	 *
	 * @param itemId 被点赞的商品 ID
	 * @return true 表示消息投递成功
	 */
	@GetMapping({ "/rocketmq", "/sentinel" })
	public boolean praise(@RequestParam Integer itemId) {
		// 1. 构造业务载荷
		PraiseMessage message = new PraiseMessage();
		message.setItemId(itemId);
		// 2. 包装为标准 Message（后续可在这里加 header，如消息追踪 ID）
		Message<PraiseMessage> praiseMessage = MessageBuilder.withPayload(message)
				.build();
		// 3. 发送到 praise-output 绑定（由 Nacos 配置映射到 RocketMQ topic）
		return streamBridge.send(BINDING_NAME, praiseMessage);
	}

}
