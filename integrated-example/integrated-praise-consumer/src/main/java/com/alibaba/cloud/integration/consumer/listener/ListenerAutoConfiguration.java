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

package com.alibaba.cloud.integration.consumer.listener;

import java.util.function.Consumer;

import com.alibaba.cloud.integration.consumer.message.PraiseMessage;
import com.alibaba.cloud.integration.consumer.service.PraiseService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * RocketMQ 消息监听配置 —— Spring Cloud Stream「函数式消费模型」核心。
 *
 * <p>工作原理（重要）：</p>
 * <ol>
 *   <li>Nacos 配置 integrated-consumer.yaml 中声明了
 *       {@code spring.cloud.stream.function.definition: consumer;}，
 *       告诉 Stream："容器里名为 consumer 的 Bean 就是我的消费函数"；</li>
 *   <li>本类提供的 Bean 恰好叫 {@code consumer()}，类型是
 *       {@code Consumer<Message<PraiseMessage>>}，Stream 启动时自动把它
 *       绑定到 consumer-in-0 输入通道（命名规则：函数名 + "-in-" + 序号）；</li>
 *   <li>consumer-in-0 在配置里映射到 topic PRAISE-TOPIC-01，
 *       于是 MQ 每来一条消息，lambda 里的逻辑就被回调一次。</li>
 * </ol>
 *
 * <p>对比老版本 @EnableBinding + @StreamListener 的注解模型，
 * 函数式模型纯 Java 函数、无框架注解侵入，是当前官方推荐写法。</p>
 */
@Configuration
public class ListenerAutoConfiguration {

	/**
	 * 定义消费函数：收到一条点赞消息 → 调用业务层给对应商品点赞数 +1。
	 *
	 * <p>方法名必须是 consumer（与配置里的 function.definition 对应），
	 * 泛型 PraiseMessage 会被自动按 JSON 反序列化。</p>
	 *
	 * @param praiseService 点赞业务服务
	 * @return 消费函数（每条消息回调一次）
	 */
	@Bean
	public Consumer<Message<PraiseMessage>> consumer(PraiseService praiseService) {
		return msg -> {
			// msg.getPayload() 即反序列化后的消息体，取出商品 ID 执行点赞
			praiseService.praiseItem(msg.getPayload().getItemId());
		};
	}
}
