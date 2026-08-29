package com.thm.cloud.integration.consumer.service;

/**
 * 点赞业务接口（消费者侧）。
 *
 * @author thm
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
