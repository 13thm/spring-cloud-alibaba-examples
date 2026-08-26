# 模块学习指南：rocketmq-tx-example（事务消息 · 分布式最终一致性）

> 角色：演示 RocketMQ 事务消息——解决"**本地事务和发消息要么都成功、要么都失败**"的经典难题。
> 难度最高、面试含金量最高的模块。

## 一、要解决什么问题（先看痛点）

场景：订单服务要 ①订单落库 ②发"订单已创建"消息给下游。
- 先发消息再落库：落库失败，下游却已经收到消息（脏消息）
- 先落库再发消息：发消息失败，下游永远不知道（丢单）
- 两阶段 DB 事务跨 MQ 做不到 → **事务消息用"半消息 + 回查"实现最终一致**

## 二、流程图（背下来就是面试答案）

```
① 发送半消息(half message)
      Producer ────────────▶ Broker：消息暂存，消费者不可见
② 执行本地事务
      Producer：落库（DB 事务）
③ 提交二次确认
      Producer ────────────▶ Broker：
        COMMIT   → 消息真正投递，消费者可见 ✅
        ROLLBACK → 删除半消息，永远不投递 ❌
        UNKNOWN  → Broker 稍后(默认1分钟)发起【回查】
④ 回查(仅 UNKNOWN 时)
      Broker ────────────▶ Producer.checkLocalTransaction()
      → 查本地事务结果，再走 ③ 的三种结果（回查默认最多15次，超过丢弃）
```

## 三、逐文件精读

### 1. application.yml —— 两个关键配置
```yaml
rocketmq:
  bindings:
    producer-out-0:
      producer:
        group: output_1
        transactionListener: myTransactionListener   # ① 指定事务监听器 Bean 名
        producerType: Trans                          # ② 该 producer 用事务发送器
```

### 2. TransactionListenerImpl.java —— 事务的两个回调（核心）
```java
@Component("myTransactionListener")
public class TransactionListenerImpl implements TransactionListener {

    // ② 执行本地事务：半消息发送成功后被回调（真正写 DB 的地方）
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        Object num = msg.getProperty("test");         // 演示用 header 值模拟三种分支
        if ("1".equals(num))  return LocalTransactionState.UNKNOW;     // 模拟"结果未知"→ 触发回查
        if ("2".equals(num))  return LocalTransactionState.ROLLBACK_MESSAGE; // 模拟本地事务失败
        return LocalTransactionState.COMMIT_MESSAGE;                    // 正常提交
    }

    // ④ 回查：Broker 对 UNKNOWN 的半消息定期回调，由你查 DB 得出最终状态
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        return LocalTransactionState.COMMIT_MESSAGE;  // 演示：回查一律提交
    }
}
```
生产代码里：`executeLocalTransaction` 执行 DB 落库并按结果返回；`checkLocalTransaction` 查订单表确认存在与否决定 COMMIT/ROLLBACK。

### 3. RocketMQTxApplication.java —— 发送侧
```java
builder.setHeader("test", String.valueOf(i))                    // 消息标记（模拟事务分支）
       .setHeader(RocketMQConst.USER_TRANSACTIONAL_ARGS, "binder"); // 传给 executeLocalTransaction 的 arg
streamBridge.send("producer-out-0", msg);                        // 事务消息照样用 StreamBridge 发
```

## 四、动手实验（对照流程图逐条验证）

```bash
# 1. 建 topic: tx，启动应用。发送 4 条消息（test=1,2,3,4），观察控制台：
```
| 消息 | executeLocalTransaction 返回 | 结果 |
|------|------------------------------|------|
| test=1 | UNKNOW | 约 1 分钟后打印 `check: ...`（Broker 回查！）→ 回查返回 COMMIT → **消费者这时才收到** |
| test=2 | ROLLBACK | 半消息删除，**消费者永远收不到**（数一数总条数少 1） |
| test=3,4 | COMMIT | 立即投递，消费者正常收到 |

**重点观察**：消费者只收到 3 条（1、3、4）；test=1 那条比发送时间晚约 1 分钟才打印——那 1 分钟就是"回查周期"。
想更快看到回查：把回查间隔调短（broker 端 `transactionTimeOut`）或把 test=1 的回查返回改成 ROLLBACK 对比。

## 五、收获

- 能完整画出半消息流程图并解释每一步
- 理解三个事务状态语义与 UNKNOWN→回查 的闭环
- 明白事务消息保证的是"**本地事务成功 ⇒ 消息必达**"（发件箱思想），不保证消费者处理成功（那是消费重试+幂等的职责）

## 六、日常/企业怎么用（真实落地）

**最经典：下单成功后通知下游**
```java
executeLocalTransaction: 订单 insert DB 成功 → COMMIT；失败 → ROLLBACK
checkLocalTransaction:    按 orderId 查订单表，存在→COMMIT，不存在→ROLLBACK
```
适用：**跨服务最终一致性**，且允许秒级延迟。
**替代方案对比**：
| 方案 | 一致性 | 复杂度 | 场景 |
|------|--------|--------|------|
| RocketMQ 事务消息 | 最终一致 | 中 | 已用 RocketMQ 的团队首选 |
| 本地消息表 + 定时扫描 | 最终一致 | 低（要建表+Job） | 不想依赖 MQ 事务特性 |
| Seata AT/TCC | 准实时/较强 | 高 | 强一致、短事务 |
| 最大努力通知 | 弱 | 低 | 对外部系统（回调+重试） |

## 七、注意什么

1. **回查逻辑必须幂等**（会被调用多次）且**查库要快**（别锁大表）
2. **UNKNOWN 滥用会堆积半消息**：回查默认 15 次后丢弃，丢弃前消费者看不到——能用 COMMIT/ROLLBACK 就别 UNKNOWN
3. 事务消息**只是发送侧的一致性**：消费者侧仍要幂等 + 异常重试，整条链路才可靠
4. 半消息也会占存储；回查期间 Producer 必须活着（回查是打到 producer group 的，**producer group 内至少一个实例在线**）
5. `USER_TRANSACTIONAL_ARGS` 是传给本地事务的参数透传（本例传了个字符串占位），真实项目常传业务上下文
6. 老写法 `@RocketMQTransactionListener` 注解与 Stream 这套配置式写法不要混用

## 八、进阶练习

- [ ] 把回查改成查"内存 Map 模拟订单表"：executeLocalTransaction 里落 Map，checkLocalTransaction 里查 Map
- [ ] 模拟 Producer 在 UNKNOWN 后宕机，观察 Broker 回查失败日志与最终丢弃
- [ ] 思考题：为什么回查是"查本地事务结果"而不是"重发消息"？（答：重发可能造成本地事务执行两次，破坏幂等）
