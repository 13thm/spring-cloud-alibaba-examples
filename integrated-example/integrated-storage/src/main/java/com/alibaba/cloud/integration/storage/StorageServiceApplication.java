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

package com.alibaba.cloud.integration.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 库存服务启动类（端口 8011）
 *
 * <p>下单场景中 Seata AT 模式的一个"分支事务"参与者（RM 角色）：</p>
 * <ul>
 *   <li>被 integrated-order 通过 Feign 调用，负责扣减库存；</li>
 *   <li>本地方法标注 {@code @Transactional}，配合 Seata 的数据源代理，
 *       把本地事务注册为全局事务的一个分支；</li>
 *   <li>全局回滚时，Seata 利用 storage 库中的 undo_log 表反向补偿数据。</li>
 * </ul>
 *
 * @author TrevorLink
 */
@SpringBootApplication
public class StorageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StorageServiceApplication.class, args);
	}

}
