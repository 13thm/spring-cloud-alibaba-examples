package com.thm.cloud.integration.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 点赞消息消费者服务启动类（端口 8024）。
 *
 * <p>点赞场景中 RocketMQ 的"消费者"一侧：</p>
 * <ul>
 *   <li>从 topic PRAISE-TOPIC-02 拉取点赞消息，按配置的速率消费（每 4 秒拉 4 条）；</li>
 *   <li>消费动作 = 更新数据库 item 表的点赞数 +1；</li>
 *   <li>同时对外提供 /praise/query 查询点赞数的接口。</li>
 * </ul>
 *
 * <p>"削峰填谷"体现在：大流量点赞瞬间堆在 MQ 里，数据库只承受
 * 消费者按固定速率产生的平滑写入压力。</p>
 *
 * @author thm
 */
@SpringBootApplication
public class PraiseConsumerApplication3 {

	public static void main(String[] args) {
		SpringApplication.run(PraiseConsumerApplication3.class, args);
	}

}
