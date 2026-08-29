package com.thm.cloud.integration.account.service;

import com.thm.cloud.integration.common.BusinessException;
import com.thm.cloud.integration.common.Result;

/**
 * 账户服务业务接口。
 *
 * @author thm
 */
public interface AccountService {

	/**
	 * 扣减账户余额（Seata 分支事务方法）。
	 * @param userId 用户 ID
	 * @param price 扣减金额
	 * @throws BusinessException 余额不足 / 扣减失败时抛出
	 */
	void reduceBalance(String userId, Integer price) throws BusinessException;

	/**
	 * 查询剩余余额。
	 * @param userId 用户 ID
	 * @return 统一响应，data 为余额
	 */
	Result<?> getRemainAccount(String userId);

}
