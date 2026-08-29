package com.thm.cloud.integration.account.service.impl;

import com.thm.cloud.integration.account.mapper.AccountMapper;
import com.thm.cloud.integration.account.service.AccountService;
import com.thm.cloud.integration.common.BusinessException;
import com.thm.cloud.integration.common.Result;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

/**
 * 账户服务实现 —— Seata AT 模式分支事务（与 StorageServiceImpl 结构对称）。
 *
 * <p>流程：打印 XID（验证全局事务传播）→ 校验余额 → 扣减 → 校验影响行数。
 * account 表初始余额 3 元、商品 2 元/件：第 1 单成功，第 2 单在
 * checkBalance 处抛"余额不足"，驱动订单服务发起全局回滚 —— 库存自动恢复。</p>
 *
 * @author thm
 */
@Service
public class AccountServiceImpl implements AccountService {

	private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

	@Autowired
	private AccountMapper accountMapper;

	/**
	 * 扣减余额（Seata 分支事务）。
	 * Seata 数据源代理自动记录 undo_log，全局回滚时反向补偿余额。
	 */
	@Override
	@Transactional
	public void reduceBalance(String userId, Integer price) throws BusinessException {
		// 打印全局事务 XID：应与订单服务一致，证明事务上下文跨服务传播成功
		logger.info("[reduceBalance] current XID: {}", RootContext.getXID());

		// 前置校验余额，不足直接抛业务异常
		checkBalance(userId, price);

		Timestamp updateTime = new Timestamp(System.currentTimeMillis());
		int updateCount = accountMapper.reduceBalance(userId, price, updateTime);
		// 影响行数为 0 → 扣减未生效（SQL 的 money >= price 条件未满足），视为失败
		if (updateCount == 0) {
			throw new BusinessException("reduce balance failed");
		}
	}

	@Override
	public Result<?> getRemainAccount(String userId) {
		Integer balance = accountMapper.getBalance(userId);
		if (balance == null) {
			return Result.failed("wrong userId, please check the userId");
		}
		return Result.success(balance);
	}

	/**
	 * 私有校验方法：余额小于扣减金额时抛 "no enough balance"。
	 * 这是触发 Seata 全局回滚的第二个"人为埋点"（第一个是库存不足）。
	 */
	private void checkBalance(String userId, Integer price) throws BusinessException {
		Integer balance = accountMapper.getBalance(userId);
		if (balance < price) {
			throw new BusinessException("no enough balance");
		}
	}

}
