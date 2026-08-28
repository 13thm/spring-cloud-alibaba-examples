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

package com.alibaba.cloud.integration.consumer.message;

/**
 * 点赞消息体（消费者侧）。
 *
 * <p>与生产者侧的 PraiseMessage 结构相同，RocketMQ 传输时以 JSON 编解码。
 * 反序列化由 Spring Cloud Stream 按配置的 content-type 自动完成，
 * 消费函数里直接拿到的就是该对象。</p>
 *
 * @author TrevorLink
 */
public class PraiseMessage {

	/** 被点赞的商品 ID */
	private Integer itemId;

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
	}

	@Override
	public String toString() {
		return "PraiseMessage{" + "itemId=" + itemId + '}';
	}

}
