/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.integration.order.controller;

import com.alibaba.cloud.integration.common.BusinessException;
import com.alibaba.cloud.integration.common.Result;
import com.alibaba.cloud.integration.order.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单服务 HTTP 接口 —— 整个"下单"场景的触发入口。
 *
 * <p>前端页面（order.html）经网关路由 {@code Path=/order/create} 打到这里，
 * 参数从表单以 query 形式传入。</p>
 *
 * @author TrevorLink
 */
@RestController
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private OrderService orderService;

	/**
	 * 创建订单（触发 Seata 全局事务）。
	 *
	 * <p>注意这里的异常处理层次：Service 内部已经把下游失败（库存/余额不足）
	 * 转换为 BusinessException 抛出 —— 该异常必须穿过 Controller 往上抛，
	 * 才能让 AOP 拦截到并触发 {@code @GlobalTransactional} 的全局回滚；
	 * Controller 捕获后再转成失败 Result 返回给前端展示。</p>
	 *
	 * @param userId 用户 ID（初始数据里是 admin）
	 * @param commodityCode 商品编码（初始数据里是 1）
	 * @param count 购买数量
	 */
	@PostMapping("/create")
	public Result<?> createOrder(@RequestParam("userId") String userId,
			@RequestParam("commodityCode") String commodityCode,
			@RequestParam("count") Integer count) {
		Result<?> res = null;
		try {
			res = orderService.createOrder(userId, commodityCode, count);
		}
		catch (BusinessException e) {
			// 全局事务已在 Service 层的 AOP 环节完成回滚，这里只是把失败信息返回给前端
			return Result.failed(e.getMessage());
		}
		return res;
	}

}
