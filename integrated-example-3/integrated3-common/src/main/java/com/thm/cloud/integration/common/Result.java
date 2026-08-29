package com.thm.cloud.integration.common;

/**
 * 统一响应结果包装类 —— 所有微服务接口出参格式统一为：
 * <pre>{@code
 * { "code": 2001, "message": "接口调用成功", "data": {...} }
 * }</pre>
 *
 * <p>好处：调用方只需判断 code 即可知道成败；微服务之间通过 Feign 调用时，
 * 也能用统一的 code 判断下游业务是否成功（见 OrderServiceImpl 中对
 * {@code getCode()} 的判断逻辑 —— 那是触发 Seata 回滚的关键一步）。</p>
 *
 * @param <T> data 字段的泛型类型
 * @author thm
 */
public class Result<T> {

	/** 结果码：2001 成功 / 2003 通用失败 */
	private Integer code;

	/** 结果描述信息 */
	private String message;

	/** 业务数据载荷，失败时通常为 null */
	private T data;

	public static <T> Result<T> success(T data) {
		return new Result<>(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMessage(), data);
	}

	public static <T> Result<T> success(String message, T data) {
		return new Result<>(ResultEnum.SUCCESS.getCode(), message, data);
	}

	public static Result<?> failed() {
		return new Result<>(ResultEnum.COMMON_FAILED.getCode(), ResultEnum.COMMON_FAILED.getMessage(), null);
	}

	/**
	 * 构造失败结果（自定义失败原因）。
	 * 最常用：Controller 捕获 {@link BusinessException} 后调用本方法。
	 */
	public static Result<?> failed(String message) {
		return new Result<>(ResultEnum.COMMON_FAILED.getCode(), message, null);
	}

	public static Result<?> failed(IResult errorResult) {
		return new Result<>(errorResult.getCode(), errorResult.getMessage(), null);
	}

	public Result() {
	}

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

}
