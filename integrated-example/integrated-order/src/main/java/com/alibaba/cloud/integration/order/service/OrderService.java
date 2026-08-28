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

package com.alibaba.cloud.integration.order.service;

import com.alibaba.cloud.integration.common.BusinessException;
import com.alibaba.cloud.integration.common.Result;

/**
 * 订单服务业务接口。
 *
 * @author TrevorLink
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
