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

package com.alibaba.cloud.integration.consumer.service;

/**
 * 点赞业务接口（消费者侧）。
 *
 * @author TrevorLink
 */
public interface PraiseService {

	/**
	 * 给商品点赞数 +1。由消息监听函数回调触发（每消费一条消息执行一次）。
	 * @param itemId 商品 ID
	 */
	void praiseItem(Integer itemId);

	/**
	 * 查询商品当前点赞数。由查询接口触发。
	 * @param itemId 商品 ID
	 * @return 点赞总数
	 */
	int getPraise(Integer itemId);

}
