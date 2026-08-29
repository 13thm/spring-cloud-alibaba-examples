package com.thm.cloud.integration.common;

/**
 * 业务异常类 —— 所有微服务"业务失败"的统一表达。
 *
 * <p>典型用法：库存不足、账户余额不足时，Service 层抛出该异常，
 * Controller 层捕获后转换为 {@link Result#failed(String)} 返回给调用方。</p>
 *
 * <p>与 Seata 的关系（重要）：在分布式事务场景下，这个异常必须从
 * {@code @GlobalTransactional} 标注的方法中抛出去，Seata TM 才会感知
 * 业务失败并向 TC 发起全局回滚 —— 各分支事务（库存、账户）随之一起回滚。</p>
 *
 * @author thm
 */
public class BusinessException extends RuntimeException {

	/**
	 * @param message 业务失败原因描述，例如 "no enough stock"
	 */
	public BusinessException(String message) {
		super(message);
	}

}
