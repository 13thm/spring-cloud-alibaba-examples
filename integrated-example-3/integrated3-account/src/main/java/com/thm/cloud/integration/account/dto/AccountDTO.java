package com.thm.cloud.integration.account.dto;

/**
 * 账户扣减请求 DTO。
 *
 * <ul>
 *   <li>userId —— 用户 ID（account 表的 user_id 字段）；</li>
 *   <li>price —— 要扣减的金额（订单服务按 数量×2 元 计算后传入）。</li>
 * </ul>
 *
 * @author thm
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
