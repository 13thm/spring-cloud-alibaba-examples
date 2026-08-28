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

package com.alibaba.cloud.integration.account.service;

import com.alibaba.cloud.integration.common.BusinessException;
import com.alibaba.cloud.integration.common.Result;

/**
 * 账户服务业务接口。
 *
 * @author TrevorLink
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
