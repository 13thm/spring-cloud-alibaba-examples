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

package com.alibaba.cloud.integration.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 点赞消息消费者服务启动类（端口 8014）
 *
 * <p>点赞场景中 RocketMQ 的"消费者"一侧：</p>
 * <ul>
 *   <li>从 topic PRAISE-TOPIC-01 拉取点赞消息，按配置的速率消费（每 4 秒拉 4 条），</li>
 *   <li>消费动作 = 更新数据库 item 表的点赞数 +1；</li>
 *   <li>同时对外提供 /praise/query 查询点赞数的接口。</li>
 * </ul>
 *
 * <p>"削峰填谷"体现在：大流量点赞瞬间堆在 MQ 里，数据库只承受
 * 消费者按固定速率产生的平滑写入压力。</p>
 *
 * @author TrevorLink
 */
@SpringBootApplication
public class PraiseConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PraiseConsumerApplication.class, args);
	}

}
