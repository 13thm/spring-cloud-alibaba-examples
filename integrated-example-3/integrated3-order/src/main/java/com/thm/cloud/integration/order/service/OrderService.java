package com.thm.cloud.integration.order.service;

import com.thm.cloud.integration.common.BusinessException;
import com.thm.cloud.integration.common.Result;

/**
 * 订单服务业务接口。
 *
 * @author thm
 */
public interface OrderService {

	/**
	 * 创建订单（全局事务入口）。
	 *
	 * <p>业务编排顺序：扣库存 → 扣余额 → 存订单；任一步失败整体回滚。</p>
	 *
	 * @param userId 用户 ID
	 * @param commodityCode 商品编码
	 * @param count 购买数量
	 * @return 成功时 data 为订单实体
	 * @throws BusinessException 库存不足 / 余额不足时抛出（触发 Seata 全局回滚）
	 */
	Result<?> createOrder(String userId, String commodityCode, Integer count)
			throws BusinessException;

}
