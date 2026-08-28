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

package com.alibaba.cloud.integration.common;

/**
 * 业务异常类
 *
 * <p>作用：本整合示例中所有微服务的"业务失败"统一用该异常表达。
 * 典型用法是：库存不足、账户余额不足时，Service 层抛出该异常，
 * 由 Controller 层捕获后转换为 {@link Result#failed(String)} 返回给调用方。</p>
 *
 * <p>与 Seata 的关系（重要）：在分布式事务场景下，这个异常必须从
 * {@code @GlobalTransactional} 标注的方法中抛出去，Seata TM 才会感知到
 * 业务失败并向 TC 发起全局回滚 —— 各分支事务（库存、账户）随之一起回滚。</p>
 *
 * @author TrevorLink
 */
public class BusinessException extends RuntimeException {

	/**
	 * @param message 业务失败原因描述，例如 "no enough balance"
	 */
	public BusinessException(String message) {
		super(message);
	}

}
