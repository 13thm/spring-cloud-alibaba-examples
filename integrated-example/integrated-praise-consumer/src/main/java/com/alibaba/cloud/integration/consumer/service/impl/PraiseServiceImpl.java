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

package com.alibaba.cloud.integration.consumer.service.impl;

import java.sql.Timestamp;

import com.alibaba.cloud.integration.consumer.mapper.PraiseMapper;
import com.alibaba.cloud.integration.consumer.service.PraiseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 点赞业务实现。
 *
 * <p>逻辑非常薄：消费到消息就 UPDATE +1，查询就 SELECT ——
 * 简单是刻意的：示例的重点在"消息如何被消费"（Stream 函数式模型、
 * 拉取速率控制），而非业务本身。</p>
 *
 * @author TrevorLink
 */
@Service
public class PraiseServiceImpl implements PraiseService {

	@Autowired
	private PraiseMapper praiseMapper;

	/**
	 * 点赞数 +1（消息消费入口）。
	 */
	@Override
	public void praiseItem(Integer itemId) {
		Timestamp updateTime = new Timestamp(System.currentTimeMillis());
		praiseMapper.praiseItem(itemId, updateTime);
	}

	/**
	 * 查询点赞数（查询接口入口）。
	 */
	@Override
	public int getPraise(Integer itemId) {
		return praiseMapper.getPraise(itemId);
	}

}
