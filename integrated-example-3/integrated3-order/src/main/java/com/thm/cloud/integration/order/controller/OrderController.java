package com.thm.cloud.integration.order.controller;

import com.thm.cloud.integration.common.BusinessException;
import com.thm.cloud.integration.common.Result;
import com.thm.cloud.integration.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单服务 HTTP 接口 —— 整个"下单"场景的触发入口。
 *
 * <p>测试时经网关路由 {@code Path=/order/create} 打到这里，
 * 参数以 query 形式传入。</p>
 *
 * @author thm
 */
@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderService orderService;

	/**
	 * 创建订单（触发 Seata 全局事务）。
	 *
	 * <p>注意异常处理的层次：Service 内部已经把下游失败（库存/余额不足）
	 * 转换为 BusinessException 抛出 —— 该异常必须穿过 Controller 往上抛，
	 * 才能让 AOP 拦截到并触发 {@code @GlobalTransactional} 的全局回滚；
	 * Controller 捕获后再转成失败 Result 返回给调用方展示。</p>
	 *
	 * @param userId 用户 ID（初始数据里是 admin）
	 * @param commodityCode 商品编码（初始数据里是 1）
	 * @param count 购买数量
	 */
	@PostMapping("/create")
	public Result<?> createOrder(@RequestParam("userId") String userId,
			@RequestParam("commodityCode") String commodityCode,
			@RequestParam("count") Integer count) {
		try {
			return orderService.createOrder(userId, commodityCode, count);
		}
		catch (BusinessException e) {
			// 全局事务已在 Service 层的 AOP 环节完成回滚，这里只是把失败信息返回
			return Result.failed(e.getMessage());
		}
	}

}
