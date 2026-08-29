package com.thm.cloud.integration.consumer.message;

/**
 * 点赞消息体（消费者侧）。
 *
 * <p>与生产者侧的 PraiseMessage 结构相同，RocketMQ 传输时以 JSON 编解码。
 * 反序列化由 Spring Cloud Stream 按配置的 content-type 自动完成，
 * 消费函数里直接拿到的就是该对象。</p>
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
