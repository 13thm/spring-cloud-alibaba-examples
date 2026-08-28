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

package com.alibaba.cloud.integration.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 账户服务启动类（端口 8012）
 *
 * <p>下单场景中 Seata AT 模式的另一个"分支事务"参与者（RM 角色）：</p>
 * <ul>
 *   <li>被 integrated-order 通过 Feign 调用，负责扣减用户账户余额；</li>
 *   <li>数据库 account 表初始余额只有 3 元（商品单价 2 元），
 *       所以第二次下单必然"余额不足"——这正是演示 Seata 全局回滚的伏笔：
 *       余额扣不了 → 抛异常 → 已扣的库存被回滚补回。</li>
 * </ul>
 *
 * @author TrevorLink
 */
@SpringBootApplication
public class AccountServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);
	}

}
