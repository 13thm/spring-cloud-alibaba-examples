package com.thm.cloud.integration.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 点赞消息生产者服务启动类（端口 8025）。
 *
 * <p>点赞场景中 RocketMQ 的"生产者"一侧：接收点赞 HTTP 请求后，
 * <b>不直接写数据库</b>，而是把点赞消息发到 RocketMQ 主题
 * PRAISE-TOPIC-02，由消费者按自己的节奏拉取消费 —— 这就是"削峰填谷"。</p>
 *
 * <p>发消息用的是 Spring Cloud Stream 的 StreamBridge（函数式编程模型），
 * 绑定与 RocketMQ 地址都在 Nacos 的 integrated2-provider.yaml 中配置。</p>
 *
 * @author thm
 */
@SpringBootApplication
public class PraiseProviderApplication3 {

	public static void main(String[] args) {
		SpringApplication.run(PraiseProviderApplication3.class, args);
	}

}
