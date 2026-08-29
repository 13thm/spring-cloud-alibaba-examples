package com.thm.cloud.integration.order.feign.dto;

/**
 * 账户扣减请求 DTO（订单服务侧的"本地副本"）。
 *
 * <p>注意：它与 integrated2-account 模块里的 AccountDTO 是两个类、内容相同 ——
 * Feign 属于跨进程 HTTP 调用，不共享类型。企业里通常抽成公共 API 模块
 * 供消费方引用，避免双份维护；本示例为保持各服务独立而各写一份。</p>
 *
 * @author thm
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
