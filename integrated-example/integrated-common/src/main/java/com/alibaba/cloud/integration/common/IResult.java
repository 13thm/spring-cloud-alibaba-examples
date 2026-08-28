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
 * 统一返回结果的顶层接口
 *
 * <p>设计意图：把"错误码 + 错误信息"抽象成接口，任何枚举/类只要实现它，
 * 就可以被 {@link Result#failed(IResult)} 直接转换为失败响应。
 * 这样后续扩展更多错误类型（比如参数错误 2002、权限不足 2004...）时，
 * 只需新增枚举实现本接口，不用改动 Result 的代码 —— 面向接口编程的典型实践。</p>
 */
public interface IResult {

	/**
	 * 获取结果码。
	 * @return 结果码，如 2001(成功) / 2003(失败)
	 */
	Integer getCode();

	/**
	 * 获取结果信息。
	 * @return 结果描述文本
	 */
	String getMessage();

}
