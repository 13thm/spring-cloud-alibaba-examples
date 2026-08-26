# 模块学习指南：rocketmq-delay-consume-example（延时消息）

> 角色：演示"发出去，但过一会才投递"的消息。一个应用自产自销。

## 一、原理一句话

> 生产端给消息打上 `delayTimeLevel` 标记 → Broker 收到后**不立刻入真实队列**，先扔进内部的延时队列（SCHEDULE_TOPIC_XXXX），定时任务到期后恢复等级、转投真实队列 → 消费者才看到。

**RocketMQ 4.x 的 18 个固定等级（必须背前几级）**：

| level | 1 | 2 | 3 | 4 | 5 | 6 | 7 | ... | 18 |
|-------|---|---|---|---|---|---|---|-----|----|
| 延迟  | 1s | **5s** | 10s | 30s | 1m | 2m | 3m | ... | 2h |

本例 `PROPERTY_DELAY_TIME_LEVEL = 2` → 延迟 **5 秒**。
（RocketMQ 5.x 支持任意时间戳的定时消息，写法不同，见官方文档。）

## 二、关键代码（就比普通消息多一行 header）

```java
Map<String, Object> headers = new HashMap<>();
headers.put(MessageConst.PROPERTY_KEYS, key);
headers.put(MessageConst.PROPERTY_DELAY_TIME_LEVEL, 2);   // ← 唯一的差别：延时等级 2 = 5秒
Message<SimpleMsg> msg = new GenericMessage<>(new SimpleMsg("Delay RocketMQ " + i), headers);
streamBridge.send("producer-out-0", msg);
```

配置无特殊项：producer-out-0 → Topic `delay`，consumer-in-0 ← Topic `delay`，group `delay-group`。

## 三、动手实验

```bash
# 1. 建 topic: delay，启动应用
# 2. 观察日志时间戳：发送完成立即打印 "send Msg"，但 consumer 每条都比发送晚 ~5 秒
# 3. 验证等级：把 2 改成 4（30s），重启观察间隔变化
# 4. 在 Dashboard 查 Topic 列表：能看到内部 Topic SCHEDULE_TOPIC_XXXX（延时的证据）
```

## 四、收获

- 理解延时消息是 **Broker 端延迟投递**，不是消费端 sleep（这是和"消费者延迟处理"的本质区别：Broker 到期才投，实例挂了也不影响到期逻辑）
- 记住等级表前 7 级；知道 4.x 只能选等级，不能任意时间
- 知道延时消息在消费端看来就是普通消息，无感知

## 五、日常/企业怎么用（延时是业务神器）

| 场景 | 做法 |
|------|------|
| **订单 30 分钟未支付自动取消** | 下单时发 30m（level 16）延时消息，到期检查支付状态，未付则关单 |
| 超时自动确认收货 | 发货时发 7 天延时消息，到期确认 |
| 重试退避 | 失败后发 10s/30s/1m 递增延时的重试消息 |
| 定时任务替代品 | 简单定时逻辑用延时消息天然分布式、随队列容量扩展，不用自己搭定时调度 |

对比自建方案：轮询 DB 扫"过期订单"（拖垮 DB）、Redis 过期监听（不可靠不精确）、Quartz/xxl-job（要额外运维）。**延时消息是最优雅的方案之一**。

## 六、注意什么

1. 4.x **只有 18 个固定等级**，你要 37 秒是做不到的（选 1m）；精确任意时间需要 5.x 或云版
2. 延时消息**同样会丢/重**：到期投递后消费失败仍走正常重试，业务要幂等
3. 大量长延时消息会占 Broker 存量，别把 MQ 当长期数据库用（几天级别的延时考虑别的方式）
4. 延时等级从 1 开始数，配 0 等于不延时

## 七、进阶练习

- [ ] 实现一个"下单 10 分钟未支付取消"的模拟：发延时消息 + 到期 Consumer 里查状态决定是否关单
- [ ] 观察同一 KEY 消息在 Dashboard 的轨迹：发送时间 vs 投递时间差 5s
