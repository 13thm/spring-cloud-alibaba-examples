package com.thm.cloud.integration.consumer.controller;

import com.thm.cloud.integration.consumer.service.PraiseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 点赞查询接口（消费者模块对外提供）。
 *
 * <p>测试时经网关（Path=/praise/query → lb://integrated2-consumer）调用本接口。</p>
 *
 * <p>注意：点赞数写入有消费延迟（消费者按 4 秒/批的节奏拉取），
 * 所以点赞后立即查询，数字可能"慢半拍"才涨上去 —— 这正是
 * MQ 异步削峰的正常现象，也是学习时值得观察的点。</p>
 *
 * @author thm
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
