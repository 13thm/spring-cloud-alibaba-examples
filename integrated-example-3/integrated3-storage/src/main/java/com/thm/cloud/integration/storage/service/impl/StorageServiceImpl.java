package com.thm.cloud.integration.storage.service.impl;

import com.thm.cloud.integration.common.BusinessException;
import com.thm.cloud.integration.common.Result;
import com.thm.cloud.integration.storage.mapper.StorageMapper;
import com.thm.cloud.integration.storage.service.StorageService;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

/**
 * 库存服务实现 —— Seata AT 模式分支事务的标准写法。
 *
 * <p>关键点：</p>
 * <ol>
 *   <li>{@code @Transactional} 是普通 Spring 本地事务注解；Seata 启动时自动
 *       代理了 DataSource，本地事务提交/回滚会同步向 TC（Seata Server）
 *       注册分支并上报状态，开发者无感知；</li>
 *   <li>{@link RootContext#getXID()}：获取全局事务 ID。日志里打印 XID
 *       可以验证"Feign 调用链上 XID 成功传播"（XID 通过请求头传递），
 *       这是 AT 模式能把多个服务的本地事务串成全局事务的基石；</li>
 *   <li>业务失败抛 {@link BusinessException}：最终让订单服务（全局事务发起方）
 *       感知失败并触发全局回滚。</li>
 * </ol>
 *
 * @author thm
 */
@Service
public class StorageServiceImpl implements StorageService {

	private static final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);

	@Autowired
	private StorageMapper storageMapper;

	/**
	 * 扣减库存（Seata 分支事务）。
	 *
	 * <p>执行流程：打印 XID → 校验库存 → 执行扣减 UPDATE → 校验影响行数。
	 * Seata 在 UPDATE 前后自动记录数据镜像到 undo_log，
	 * 全局回滚时按镜像反向补偿（把库存加回去）。</p>
	 */
	@Override
	@Transactional
	public void reduceStock(String commodityCode, Integer count) throws BusinessException {
		// 打印全局事务 ID：应与订单服务 createOrder 中的 XID 一致，证明事务上下文已跨服务传播
		logger.info("[reduceStock] current XID: {}", RootContext.getXID());

		// 前置校验：库存不足直接抛业务异常（此时还未修改数据）
		checkStock(commodityCode, count);

		Timestamp updateTime = new Timestamp(System.currentTimeMillis());
		int updateCount = storageMapper.reduceStock(commodityCode, count, updateTime);
		// UPDATE 影响行数为 0 → 扣减未生效，同样视为业务失败
		if (updateCount == 0) {
			throw new BusinessException("deduct stock failed");
		}
	}

	@Override
	public Result<?> getRemainCount(String commodityCode) {
		Integer stock = storageMapper.getStock(commodityCode);
		if (stock == null) {
			return Result.failed("commodityCode wrong, please check commodity code");
		}
		return Result.success(stock);
	}

	/**
	 * 私有校验方法：查询当前库存并与扣减数量比较。
	 * 库存不足抛异常，是本示例触发 Seata 全局回滚的"人为埋点"之一。
	 */
	private void checkStock(String commodityCode, Integer count) throws BusinessException {
		Integer stock = storageMapper.getStock(commodityCode);
		if (stock < count) {
			throw new BusinessException("no enough stock");
		}
	}

}
