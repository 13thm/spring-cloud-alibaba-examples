# 模块学习指南：rocketmq-orderly-consume-example（顺序消息）

> 角色：演示"分区顺序消息"：**同一业务 key 的消息严格先进先出**。
> 这是 RocketMQ 最有特色的特性之一，也是面试高频。

## 一、原理（三步缺一不可）

```
① 发送端：MessageQueueSelector 把同 key 的消息选进同一条 Queue
      id % tags数 % 队列数 = 固定 index → 同 id 永远同一队列
② Broker：Queue 内天然 FIFO 存储
③ 消费端：orderly=true，单线程逐条消费该队列（失败会阻塞重试本条，不跳过）
```
**顺序的粒度是 Queue（分区）级别**：不同 key 之间乱序没关系，同 key 内部严格有序。
（全局顺序 = Topic 只建 1 个队列，吞吐极差，几乎不用。）

## 二、逐文件精读

### 1. OrderlyMessageQueueSelector.java —— 顺序的源头
```java
@Component
public class OrderlyMessageQueueSelector implements MessageQueueSelector {
    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        Integer id = (Integer) ((MessageHeaders) arg).get(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID);
        int index = id % RocketMQOrderlyConsumeApplication.tags.length % mqs.size();
        return mqs.get(index);      // 同 id → 永远同一条队列
    }
}
```
**关键点**：选择算法必须**确定性**（同 key 永远算出同一队列）。生产上通常用 `shardingKey.hashCode() % mqs.size()`，
比如 orderId、userId 作为 sharding key——"同一订单的消息顺序处理"就靠它。

### 2. application.yml —— 两端配置
```yaml
rocketmq:
  bindings:
    producer-out-0:
      producer:
        group: output_1
        messageQueueSelector: orderlyMessageQueueSelector   # ① 注册自定义选择器（Bean 名）
    consumer-in-0:
      consumer:
        subscription: 'TagA || TagC || TagD'                # ② 顺带演示 Tag 过滤（本实验会漏掉 TagB/E 的消费）
        push:
          orderly: true                                     # ③ 消费端顺序模式（单线程/队列，失败重试不跳过）
```

### 3. 主类 —— 生产消费一体
- producer：100 条消息，header 带 `TAGS = TagA..TagE 循环`、`ORIGIN_MESSAGE_ID = i`
- consumer：打印消息 + TAG，故意 `Thread.sleep(100)` 模拟慢处理（顺序消费时的"串行"看得更清楚）

## 三、动手实验

```bash
# 1. 建 topic: orderly（注意默认队列数 8，读队列=写队列）
# 2. 启动应用，观察日志：
```
- **验证发送端**：Dashboard 里看 Topic `orderly` 各 Queue 的消息分布——序号 i 固定落在某条队列
- **验证消费端**：消费日志中 TAG 交替出现，但同一 TAG 的 ORIGIN_MESSAGE_ID 严格递增（TagA 的 0,5,10,15...顺序不变）
- **实验 A**：把 `orderly: true` 改成 false（并发消费）→ 多线程并发消费，同 TAG 序号可能乱（消费端乱序）
- **实验 B**：去掉自定义 Selector（普通发送轮询进队列）→ 同 TAG 消息散落多条队列，消费顺序无法保证（发送端乱序）

**结论**：顺序 = 发送端选队列 + 消费端串行，**两头都要配**，缺一个就乱序。

## 四、收获

- 理解"全局顺序 vs 分区顺序"，以及为什么生产只用分区顺序
- 会写/配 MessageQueueSelector（sharding key 选队列）
- 理解消费端 orderly 模式的行为：**失败会卡住当前队列重试**（防止跳序），和并发模式"失败重试可能乱序"的区别

## 五、日常/企业怎么用

| 场景 | sharding key |
|------|--------------|
| 订单状态流转（创建→支付→发货→完成） | orderId |
| 同一用户的操作流水（注册→登录→下单） | userId |
| 库存变更流水 | skuId |
| 数据库 binlog 同步（保持同表/同行变更顺序） | 表名/主键 |

**判断口诀**：这批消息如果"后到的先处理"会造成业务错乱 → 用顺序消息；只是展示先后无业务影响 → 普通消息（吞吐高得多）。

## 六、注意什么

1. **顺序的代价是吞吐**：消费端串行 + 热点 key 全挤一条队列。热点大卖家订单全进一队会成为瓶颈
2. **消费失败会阻塞**该队列后续消息（重试默认 Integer.MAX_VALUE 次）——顺序消费的异常处理要格外小心，毒丸消息会堵死队列
3. **扩缩容队列数要谨慎**：`hash % 队列数`，队列数一变，旧消息与新消息可能落到不同队列，短暂乱序
4. consumer 的 `subscription` 只订了 TagA/C/D，TagB/E 的消息没人消费（演示过滤的副作用），生产上注意订阅完整性
5. 重试时消息依然有序，但**消费端务必幂等**（顺序模式失败重投会重复）

## 七、进阶练习

- [ ] 把 Selector 的 key 改成模拟 orderId（`i / 10`），验证"同一订单的 10 条消息"严格有序
- [ ] 在 consumer 里抛一次异常，观察顺序模式下重试阻塞行为 vs 并发模式的差别
