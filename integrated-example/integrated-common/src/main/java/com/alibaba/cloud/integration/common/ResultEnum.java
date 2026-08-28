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
 * Integrated Example 统一返回码枚举。
 *
 * <p>实现 {@link IResult} 接口后，即可直接传入
 * {@link Result#failed(IResult)} 生成失败响应。</p>
 *
 * <p>注意：这里成功码是 2001 而非习惯上的 200 —— 这是业务码不是 HTTP 状态码，
 * HTTP 层面请求仍是 200 OK，业务成败由 body 中的 code 表达。</p>
 *
 * @author TrevorLink
 */
public enum ResultEnum implements IResult {

	/**
	 * 接口调用成功。
	 */
	SUCCESS(2001, "接口调用成功"),
	/**
	 * 接口调用通用失败（库存不足、余额不足等业务失败最终都映射到该码）。
	 */
	COMMON_FAILED(2003, "接口调用失败");

	/** 业务结果码 */
	private Integer code;

	/** 结果描述 */
	private String message;

	ResultEnum() {
	}

	ResultEnum(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
