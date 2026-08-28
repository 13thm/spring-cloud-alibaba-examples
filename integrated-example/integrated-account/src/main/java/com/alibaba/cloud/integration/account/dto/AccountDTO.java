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

package com.alibaba.cloud.integration.account.dto;

/**
 * 账户扣减请求 DTO。
 *
 * <ul>
 *   <li>userId —— 用户 ID（account 表的 user_id 字段）；</li>
 *   <li>price —— 要扣减的金额（订单服务按 数量×2 元 计算后传入）。</li>
 * </ul>
 *
 * @author TrevorLink
 */
public class AccountDTO {

	/** 用户 ID */
	private String userId;

	/** 扣减金额（单位：元） */
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
