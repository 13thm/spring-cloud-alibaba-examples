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

package com.alibaba.cloud.integration.storage.service;

import com.alibaba.cloud.integration.common.BusinessException;
import com.alibaba.cloud.integration.common.Result;

/**
 * 库存服务业务接口。
 *
 * @author TrevorLink
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
