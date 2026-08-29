package com.thm.cloud.integration.order.feign.dto;

/**
 * 库存扣减请求 DTO（订单服务侧的"本地副本"，与 integrated2-storage 的 StorageDTO 内容相同）。
 *
 * @author thm
 */
public class StorageDTO {

	/** 商品编码 */
	private String commodityCode;

	/** 扣减数量 */
	private Integer count;

	public String getCommodityCode() {
		return commodityCode;
	}

	public void setCommodityCode(String commodityCode) {
		this.commodityCode = commodityCode;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

}
