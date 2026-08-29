package com.thm.cloud.integration.common;

/**
 * 统一响应结果枚举，实现 {@link IResult} 后可直接传入
 * {@link Result#failed(IResult)} 生成失败响应。
 *
 * <p>注意：成功码是 2001 而非习惯上的 200 —— 这是业务码不是 HTTP 状态码，
 * HTTP 层面请求仍是 200 OK，业务成败由响应 body 中的 code 字段表达。</p>
 *
 * @author thm
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

	private final Integer code;

	private final String message;

	ResultEnum(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public Integer getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
