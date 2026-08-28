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

package com.alibaba.cloud.integration.consumer.controller;

import com.alibaba.cloud.integration.consumer.service.PraiseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 点赞查询接口（消费者模块对外提供）。
 *
 * <p>前端 rocketmq.html 页面"查询点赞数"按钮经网关
 * （Path=/praise/query → lb://integrated-consumer）调用本接口。</p>
 *
 * <p>注意：点赞数写入有消费延迟（消费者按 4 秒/批的节奏拉取），
 * 所以点赞后立即查询，数字可能"慢半拍"才涨上去 —— 这正是
 * MQ 异步削峰的正常现象，也是学习时值得观察的点。</p>
 *
 * @author TrevorLink
 */
@RestController
@RequestMapping("/praise")
public class PraiseController {

	@Autowired
	private PraiseService praiseService;

	/**
	 * 查询指定商品的点赞数。
	 * @param itemId 商品 ID
	 * @return 当前点赞总数（直接返回 Integer，未包 Result）
	 */
	@GetMapping("/query")
	public Integer getPraise(Integer itemId) {
		return praiseService.getPraise(itemId);
	}

}
