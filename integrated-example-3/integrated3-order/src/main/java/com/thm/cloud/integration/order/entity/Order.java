package com.thm.cloud.integration.order.entity;

import java.sql.Timestamp;

/**
 * 订单实体类，对应 integrated2_order 库的 {@code order} 表。
 *
 * <p>一张订单记录一次下单：谁(userId)买了什么(commodityCode)、
 * 买了几件(count)、花了多少钱(money=单价2元×count)。</p>
 *
 * @author thm
 */
public class Order {

	/** 主键，自增；插入后由 MyBatis 回填（见 OrderMapper @Options） */
	private Integer id;

	/** 下单用户 ID */
	private String userId;

	/** 商品编码 */
	private String commodityCode;

	/** 购买数量 */
	private Integer count;

	/** 订单金额（= 数量 × 2 元） */
	private Integer money;

	/** 创建时间 */
	private Timestamp createTime;

	/** 更新时间 */
	private Timestamp updateTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

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

	public Integer getMoney() {
		return money;
	}

	public void setMoney(Integer money) {
		this.money = money;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public String toString() {
		return "Order{id=" + id + ", userId='" + userId + "', commodityCode='" + commodityCode
				+ "', count=" + count + ", money=" + money + '}';
	}

}
