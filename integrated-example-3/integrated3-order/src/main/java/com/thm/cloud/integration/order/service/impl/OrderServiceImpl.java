package com.thm.cloud.integration.order.service.impl;

import com.thm.cloud.integration.common.BusinessException;
import com.thm.cloud.integration.common.Result;
import com.thm.cloud.integration.common.ResultEnum;
import com.thm.cloud.integration.order.entity.Order;
import com.thm.cloud.integration.order.feign.AccountServiceFeignClient;
import com.thm.cloud.integration.order.feign.StorageServiceFeignClient;
import com.thm.cloud.integration.order.feign.dto.AccountDTO;
import com.thm.cloud.integration.order.feign.dto.StorageDTO;
import com.thm.cloud.integration.order.mapper.OrderMapper;
import com.thm.cloud.integration.order.service.OrderService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * 订单服务实现 —— <b>整个分布式事务示例的核心类</b>。
 *
 * <p>{@code @GlobalTransactional}（Seata 全局事务注解）加在这个方法上，
 * AOP 拦截后的完整时序：</p>
 *
 * <pre>
 * 1. TM 向 TC(Seata Server) 申请开启全局事务，获得全局唯一 XID
 * 2. Feign 调库存服务（请求头携带 XID）→ 库存本地事务注册为分支①
 * 3. Feign 调账户服务（请求头携带 XID）→ 账户本地事务注册为分支②
 * 4. 本地插入订单（同一数据源代理）→ 注册为分支③
 * 5. 方法正常返回 → TM 通知 TC 全局提交，各分支删除 undo_log（异步）
 *    方法抛出异常 → TM 通知 TC 全局回滚，各分支按 undo_log 反向补偿
 * </pre>
 *
 * <p>关键点：下游"业务失败"（如余额不足）经 Feign 拿到的是 code=2003 的
 * 正常 HTTP 响应（不会自动抛异常），所以这里必须<b>手动判断 code 并抛出
 * BusinessException</b>，让 AOP 感知失败触发回滚 —— 最容易被忽略的细节。</p>
 *
 * @author thm
 */
@Service
public class OrderServiceImpl implements OrderService {

	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

	@Autowired
	private OrderMapper orderMapper;

	/** 库存服务 Feign 客户端（发 HTTP 调用 integrated2-storage） */
	@Autowired
	private StorageServiceFeignClient storageService;

	/** 账户服务 Feign 客户端（发 HTTP 调用 integrated2-account） */
	@Autowired
	private AccountServiceFeignClient accountService;

	/**
	 * 创建订单 —— Seata 全局事务入口（TM 角色）。
	 *
	 * <p>演示剧本（配合初始数据：库存 100 件、余额 3 元、商品 2 元/件）：</p>
	 * <ul>
	 *   <li>买 1 件：扣库存成功 → 扣余额 2 元成功 → 订单落库，全局提交；</li>
	 *   <li>再买 1 件：扣库存成功 → 扣余额失败（只剩 1 元）→ 抛异常，
	 *       Seata 全局回滚：<b>第 2 步已扣的库存被 undo_log 补回 100</b>，
	 *       三张表数据最终一致。</li>
	 * </ul>
	 */
	@Override
	@GlobalTransactional
	public Result<?> createOrder(String userId, String commodityCode, Integer count) {

		// 打印全局事务 XID：库存/账户服务日志中的 XID 应与此一致，证明事务上下文跨服务传播
		logger.info("[createOrder] current XID: {}", RootContext.getXID());

		// ---------- 第 1 步：扣减库存（Feign → integrated2-storage） ----------
		StorageDTO storageDTO = new StorageDTO();
		storageDTO.setCommodityCode(commodityCode);
		storageDTO.setCount(count);
		Integer storageCode = storageService.reduceStock(storageDTO).getCode();
		// Feign 拿到的是"正常 HTTP 响应里的业务失败码"，必须手动判断并抛异常，
		// 才能触发 @GlobalTransactional 的回滚逻辑
		if (storageCode.equals(ResultEnum.COMMON_FAILED.getCode())) {
			throw new BusinessException("stock not enough");
		}

		// ---------- 第 2 步：扣减账户余额（Feign → integrated2-account） ----------
		int price = count * 2;  // 商品单价固定 2 元，总价 = 数量 × 2
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setUserId(userId);
		accountDTO.setPrice(price);
		Integer accountCode = accountService.reduceBalance(accountDTO).getCode();
		// 余额不足 → 抛异常触发全局回滚（演示时第二次下单走的就是这里）
		if (accountCode.equals(ResultEnum.COMMON_FAILED.getCode())) {
			throw new BusinessException("balance not enough");
		}

		// ---------- 第 3 步：保存订单（本地事务，也注册为全局事务分支） ----------
		Order order = new Order();
		order.setUserId(userId);
		order.setCommodityCode(commodityCode);
		order.setCount(count);
		order.setMoney(price);
		order.setCreateTime(new Timestamp(System.currentTimeMillis()));
		order.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		orderMapper.saveOrder(order);
		// id 已由 @Options(useGeneratedKeys) 回填，可直接打印
		logger.info("[createOrder] order saved: {}", order);

		return Result.success(order);
	}

}
