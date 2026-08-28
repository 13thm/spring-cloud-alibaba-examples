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
 * 统一响应结果包装类
 *
 * <p>整个整合示例中，所有微服务接口的出参格式统一为：</p>
 * <pre>{@code
 * { "code": 2001, "message": "接口调用成功", "data": {...} }
 * }</pre>
 *
 * <p>这样做的好处：前端/调用方只需判断 code 即可知道成败；
 * 微服务之间通过 Feign 调用时，也能用统一的 code 判断下游业务是否成功
 * （见 OrderServiceImpl 中对 {@code getCode()} 的判断逻辑）。</p>
 *
 * @param <T> data 字段的泛型类型
 * @author TrevorLink
 */
public class Result<T> {

	/** 结果码：2001 成功 / 2003 通用失败 */
	private Integer code;

	/** 结果描述信息 */
	private String message;

	/** 业务数据载荷，失败时通常为 null */
	private T data;

	/**
	 * 构造成功结果（使用默认成功文案）。
	 * @param data 业务数据
	 * @return 成功响应
	 */
	public static <T> Result<T> success(T data) {
		return new Result<>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(),
				data);
	}

	/**
	 * 构造成功结果（自定义文案）。
	 * @param message 自定义成功描述
	 * @param data 业务数据
	 * @return 成功响应
	 */
	public static <T> Result<T> success(String message, T data) {
		return new Result<>(ResultEnum.SUCCESS.getCode(), message, data);
	}

	/**
	 * 构造失败结果（使用默认失败文案 "接口调用失败"）。
	 * @return 失败响应
	 */
	public static Result<?> failed() {
		return new Result<>(ResultEnum.COMMON_FAILED.getCode(),
				ResultEnum.COMMON_FAILED.getMessage(), null);
	}

	/**
	 * 构造失败结果（自定义失败原因）。
	 * 最常用：Controller 捕获 {@link BusinessException} 后调用本方法。
	 * @param message 失败原因
	 * @return 失败响应
	 */
	public static Result<?> failed(String message) {
		return new Result<>(ResultEnum.COMMON_FAILED.getCode(), message, null);
	}

	/**
	 * 构造失败结果（错误码来自任意 IResult 实现，便于扩展更多错误类型）。
	 * @param errorResult 错误码枚举
	 * @return 失败响应
	 */
	public static Result<?> failed(IResult errorResult) {
		return new Result<>(errorResult.getCode(), errorResult.getMessage(), null);
	}

	public Result() {
	}

	/**
	 * 全参构造器，被各个静态工厂方法复用。
	 */
	public Result(Integer code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	/**
	 * 通用实例方法：手动 set 三个字段的另一种写法（本项目主要用静态工厂方法）。
	 */
	public static <T> Result<T> instance(Integer code, String message, T data) {
		Result<T> result = new Result<>();
		result.setCode(code);
		result.setMessage(message);
		result.setData(data);
		return result;
	}

}
