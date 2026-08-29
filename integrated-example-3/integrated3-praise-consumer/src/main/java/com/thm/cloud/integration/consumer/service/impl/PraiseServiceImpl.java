package com.thm.cloud.integration.consumer.service.impl;

import com.thm.cloud.integration.consumer.mapper.PraiseMapper;
import com.thm.cloud.integration.consumer.service.PraiseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * 点赞业务实现。
 *
 * <p>逻辑非常薄：消费到消息就 UPDATE +1，查询就 SELECT ——
 * 简单是刻意的：示例的重点在"消息如何被消费"（Stream 函数式模型、
 * 拉取速率控制），而非业务本身。</p>
 *
 * @author thm
 */
@Service
public class PraiseServiceImpl implements PraiseService {

	@Autowired
	private PraiseMapper praiseMapper;

	@Override
	public void praiseItem(Integer itemId) {
		Timestamp updateTime = new Timestamp(System.currentTimeMillis());
		praiseMapper.praiseItem(itemId, updateTime);
	}

	@Override
	public int getPraise(Integer itemId) {
		return praiseMapper.getPraise(itemId);
	}

}
