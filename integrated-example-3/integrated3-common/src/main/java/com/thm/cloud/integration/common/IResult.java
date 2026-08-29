package com.thm.cloud.integration.common;

/**
 * 统一返回结果的顶层接口。
 *
 * <p>设计意图：把"错误码 + 错误信息"抽象成接口，任何枚举/类只要实现它，
 * 就可以被 {@link Result#failed(IResult)} 直接转换为失败响应。
 * 后续扩展更多错误类型（参数错误 2002、权限不足 2004...）时，
 * 只需新增枚举实现本接口，不用改动 Result 的代码。</p>
 *
 * @author thm
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
