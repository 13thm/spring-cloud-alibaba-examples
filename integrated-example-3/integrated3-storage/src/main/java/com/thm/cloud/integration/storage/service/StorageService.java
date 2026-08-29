package com.thm.cloud.integration.storage.service;

import com.thm.cloud.integration.common.BusinessException;
import com.thm.cloud.integration.common.Result;

/**
 * 库存服务业务接口。
 *
 * @author thm
 */
public interface StorageService {

	/**
	 * 扣减库存（Seata 分支事务方法）。
	 * @param commodityCode 商品编码
	 * @param orderCount 本次下单数量
	 * @throws BusinessException 库存不足 / 更新失败时抛出
	 */
	void reduceStock(String commodityCode, Integer orderCount) throws BusinessException;

	/**
	 * 查询剩余库存。
	 * @param commodityCode 商品编码
	 * @return 统一响应，data 为剩余数量
	 */
	Result<?> getRemainCount(String commodityCode);

}
