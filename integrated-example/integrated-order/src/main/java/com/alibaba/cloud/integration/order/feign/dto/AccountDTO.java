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

package com.alibaba.cloud.integration.order.feign.dto;

/**
 * 账户扣减请求 DTO（订单服务侧的"本地副本"）。
 *
 * <p>注意：它与 integrated-account 模块里的 AccountDTO 是两个类、内容相同 ——
 * Feign 属于跨进程 HTTP 调用，不共享类型。企业里通常抽成公共 API 模块
 * 供消费方引用，避免双份维护；本示例为保持各服务独立而各写一份。</p>
 *
 * @author TrevorLink
 */
public class AccountDTO {

	/** 用户 ID */
	private String userId;

	/** 扣减金额 */
	private Integer price;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

}
