package com.thm.cloud.integration.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 库存服务启动类（端口 8021）。
 *
 * <p>下单场景中 Seata AT 模式的一个"分支事务"参与者（RM 角色）：</p>
 * <ul>
 *   <li>被 integrated2-order 通过 Feign 调用，负责扣减库存；</li>
 *   <li>本地方法标注 {@code @Transactional}，配合 Seata 的数据源代理，
 *       把本地事务注册为全局事务的一个分支；</li>
 *   <li>全局回滚时，Seata 利用 storage 库中的 undo_log 表反向补偿数据。</li>
 * </ul>
 *
 * @author thm
 */
@SpringBootApplication
public class StorageServiceApplication3 {

	public static void main(String[] args) {
		SpringApplication.run(StorageServiceApplication3.class, args);
	}

}
