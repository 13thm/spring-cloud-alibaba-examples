package com.thm.cloud.integration.storage.dto;

/**
 * 库存扣减请求 DTO（数据传输对象）。
 *
 * <p>字段与 storage 表对应：</p>
 * <ul>
 *   <li>commodityCode —— 商品编码（storage 表的唯一键）；</li>
 *   <li>count —— 要扣减的数量。</li>
 * </ul>
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
