package com.thm.cloud.integration.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 账户服务启动类（端口 38022）。
 *
 * <p>下单场景中 Seata AT 模式的另一个"分支事务"参与者（RM 角色）：</p>
 * <ul>
 *   <li>被 integrated2-order 通过 Feign 调用，负责扣减用户账户余额；</li>
 *   <li>account 表初始余额只有 3 元（商品单价 2 元），第二次下单必然
 *       "余额不足"——这正是演示 Seata 全局回滚的伏笔：
 *       余额扣不了 → 抛异常 → 已扣的库存被回滚补回。</li>
 * </ul>
 *
 * @author thm
 */
@SpringBootApplication
public class AccountServiceApplication3 {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication3.class, args);
	}

}
