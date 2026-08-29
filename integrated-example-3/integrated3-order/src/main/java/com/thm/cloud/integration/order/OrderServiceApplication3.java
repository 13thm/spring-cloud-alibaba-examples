package com.thm.cloud.integration.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 订单服务启动类（端口 38023）。
 *
 * <p>下单链路的"总指挥"，承担两个关键角色：</p>
 * <ul>
 *   <li><b>Seata TM（事务发起者）</b>：{@code OrderServiceImpl#createOrder}
 *       标注了 {@code @GlobalTransactional}，是全局事务的起点，
 *       开启全局事务、传播 XID、并根据成败决定全局提交/回滚；</li>
 *   <li><b>Feign 消费方</b>：通过两个 Feign 接口编排调用库存服务与账户服务。</li>
 * </ul>
 *
 * @author thm
 */
@SpringBootApplication
// 开启 Feign 客户端扫描：为 feign 包下的 @FeignClient 接口生成代理实现并注入容器
@EnableFeignClients
public class OrderServiceApplication3 {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication3.class, args);
	}

}
