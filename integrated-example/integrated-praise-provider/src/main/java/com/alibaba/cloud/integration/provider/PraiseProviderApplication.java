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

package com.alibaba.cloud.integration.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 点赞消息生产者服务启动类（端口 8015）
 *
 * <p>点赞场景中 RocketMQ 的"生产者"一侧：接收点赞 HTTP 请求后，
 * <b>不直接写数据库</b>，而是把点赞消息发到 RocketMQ 主题
 * PRAISE-TOPIC-01，由消费者按自己的节奏拉取消费 —— 这就是"削峰填谷"。</p>
 *
 * <p>发消息用的是 Spring Cloud Stream 的 StreamBridge（函数式编程模型），
 * 绑定与 RocketMQ 地址都在 Nacos 的 integrated-provider.yaml 中配置，
 * 本地代码零 MQ 相关硬编码。</p>
 *
 * @author TrevorLink
 */
@SpringBootApplication
public class PraiseProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(PraiseProviderApplication.class, args);
	}

}
