package com.thm.cloud.integration.provider.message;

/**
 * 点赞消息体（生产者侧）。
 *
 * <p>经 RocketMQ 传输时按 JSON 序列化（见 Nacos 配置中 content-type: application/json），
 * 消费者侧用同结构的 PraiseMessage 反序列化。生产/消费两端各自维护一份
 * 消息体类，是消息驱动架构的常见做法（也可抽公共消息模块）。</p>
 *
 * @author thm
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
		return "PraiseMessage{itemId=" + itemId + '}';
	}

}
