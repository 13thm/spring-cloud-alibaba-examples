# RocketMQ + Spring Cloud Stream 学习路线总览

> 本目录演示：**用 Spring Cloud Stream 统一的函数式 API 操作 RocketMQ**，覆盖 7 种消息形态：
> 基础收发、广播、顺序、延时、SQL 过滤、轮询消费、事务消息。

---

## 一、先建立宏观认知（3 个层次）

```
你的代码（只写业务函数）          Spring Cloud Stream（翻译层）        RocketMQ（真正干活）
┌────────────────────┐      ┌──────────────────────────┐      ┌──────────────────┐
│ Supplier<T> 发消息  │─────▶│ Binding: xxx-out-0        │─────▶│ Topic（消息的     │
│ Consumer<T> 收消息  │◀─────│ Binding: xxx-in-0         │      │  分类/频道）       │
│ Function<T,R> 处理  │◀──▶  │ Binder: 连接 RocketMQ     │      │ Queue（分区内队列）│
└────────────────────┘      └──────────────────────────┘      │ Consumer Group   │
       ↑ 不 import 任何 RocketMQ 类（几乎）                        └──────────────────┘
```

**三个核心概念（必背）**：

| 概念 | 类比 | 说明 |
|------|------|------|
| **Destination** | 电视频道 | 即 RocketMQ 的 Topic，如 `broadcast`、`tx`。发到同一 Topic 的消息都能被订阅方看到 |
| **Binding** | 电视机的接线 | 把你的函数和 Topic 接起来。命名规则：`<函数名>-out-0`（发）/ `<函数名>-in-0`（收） |
| **Binder** | 不同制式的插座 | 屏蔽中间件差异：换成 Kafka/RabbitMQ，业务代码几乎不动，只换 starter + 配置 |

**Group（消费组）的黄金法则（最重要的一条）**：
> 同一个 Group 内的多实例**竞争消费**（一条消息只被组内一个实例处理，天然负载均衡）；
> 不同 Group 各自拿到**全量消息**（互不影响）。

## 二、模块地图与学习路线（建议顺序，5~7 天）

| 顺序 | 模块 | 学什么 | 难度 |
|------|------|--------|------|
| 0 | `rocketmq-example-common` | 共享消息体 SimpleMsg | ⭐ |
| 1 | `rocketmq-comprehensive-example` | **地基**：Supplier/Function/Consumer 函数式三件套 + Binding 配置 | ⭐⭐ |
| 2 | `rocketmq-broadcast-example` | 集群 vs 广播两种消费模式；验证 Group 法则 | ⭐⭐ |
| 3 | `rocketmq-delay-consume-example` | 延时消息（延迟投递） | ⭐⭐ |
| 4 | `rocketmq-sql-consume-example` | Tag/SQL92 消息过滤 | ⭐⭐ |
| 5 | `rocketmq-orderly-consume-example` | 顺序消息 + MessageQueueSelector | ⭐⭐⭐ |
| 6 | `rocketmq-pollable-consume-example` | 主动拉取式消费（poll） | ⭐⭐ |
| 7 | `rocketmq-tx-example` | 事务消息（最终一致性，面试重点） | ⭐⭐⭐⭐ |

## 三、环境准备（所有实验的前提）

1. 下载 RocketMQ 二进制包并启动：
```bash
sh bin/mqnamesrv                                # 启动 NameServer（电话簿）
sh bin/mqbroker -n localhost:9876              # 启动 Broker（真正存消息的）
```
2. 为各实验创建 Topic（生产建议关闭自动建 Topic，手动规范创建）：
```bash
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t broadcast
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t delay
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t sql
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t orderly
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t tx
sh bin/mqadmin updateTopic -n localhost:9876 -c DefaultCluster -t pollable
```
3. （SQL 过滤实验需要）broker 配置文件加 `enablePropertyFilter=true` 后重启 broker。
4. 可选：装 RocketMQ Dashboard，图形化看 Topic/消息/消费进度，学习神器。

## 四、7 个实验一图流（做完你就毕业了）

| 实验 | 启动什么 | 观察什么 |
|------|----------|----------|
| ① comprehensive | 单应用 | 每 2 秒 supplier 产一条 → processor 平方加工 → consumer 打印。理解**流式管道** |
| ② broadcast | 1 生产者 + 2 消费者 | 两个消费者**都**收到全部 100 条（广播模式）；去掉 BROADCASTING 再跑 → 变成瓜分（集群模式） |
| ③ delay | 单应用 | 发送后**约 5 秒**才打印（等级 2=5s），发送与消费时间差 |
| ④ sql | 单应用 | 只收到 color∈{red1,red2,red4} **且** price>3 的消息（约 60 条而非 100 条） |
| ⑤ orderly | 单应用 | 消费日志中同一 TAG 的消息 id 严格递增；Selector 保证同 key 进同一队列 |
| ⑥ pollable | 单应用 | 自己起线程 `poll()` 拉消息，节奏自己控制，没有消息就 sleep 1s |
| ⑦ tx | 单应用 | 4 条消息：1 条 UNKNOWN 触发**回查**后 commit、1 条 ROLLBACK **消失**、2 条正常 commit |

## 五、学完应收获什么（Checklist）

- [ ] 能说清 Binder/Binding/Destination/Group 四个概念
- [ ] 会用 `@Bean Supplier/Consumer/Function` + `function.definition` + `<name>-in/out-0` 配置完成收发
- [ ] 会用 `StreamBridge.send("xxx-out-0", msg)` 任意位置发消息
- [ ] 能解释集群模式和广播模式的区别及各自适用场景
- [ ] 理解顺序消息的原理：**同 key 进同队列 + 队列内 FIFO + 消费端单线程**
- [ ] 理解延时等级（4.x 固定 18 级，level2=5s；5.x 支持任意时刻）
- [ ] 理解事务消息的半消息机制和三个状态（COMMIT/ROLLBACK/UNKNOWN→回查）

## 六、日常怎么用 / 企业怎么用

**为什么需要 MQ（三大价值，面试必答）**：
1. **异步**：下单后发消息就返回，积分/短信/库存异步处理，接口耗时从 500ms 降到 50ms
2. **解耦**：订单服务发"订单已创建"事件，关心此事的服务自行订阅，新增订阅方零改动
3. **削峰**：秒杀 10 万请求先进 MQ 排队，消费端按自己的处理能力匀速消费

**企业典型落地（每个特性对应真实场景）**：
| 特性 | 真实用途 |
|------|----------|
| 普通消息 | 订单事件通知、日志采集、用户行为埋点 |
| 广播 | **多实例本地缓存同步刷新**、配置推送（每台机器都要更新） |
| 顺序 | 同一订单的状态流转（创建→支付→发货）必须有序 |
| 延时 | **30 分钟未支付自动取消订单**、超时确认收货、重试退避 |
| SQL/Tag 过滤 | 同一 Topic 按业务方订阅不同子集，减少无用流量 |
| 事务消息 | "DB 落库 + 发消息"要么都成功要么都失败（最终一致） |

## 七、注意事项（血泪教训）

1. **消费必须幂等**：MQ 是 at-least-once，重试/重平衡会导致重复投递。用消息 KEY/业务唯一键做去重表
2. **消息不丢三连问**：发端用同步发送+失败重试；Broker 主从同步刷盘；消费端业务成功后才 ACK（别在消费逻辑里 catch 掉异常却返回成功）
3. **积压是头号事故**：消费太慢 → 消息堆积。消费逻辑里**严禁**同步调慢接口/大事务，必要时加机器并行消费
4. **group 命名要规范**：`{业务域}-{用途}-group`，一个 group 只订阅一个 Topic（否则订阅关系混乱，经典大坑）
5. **生产禁用自动创建 Topic**：Topic 数量影响性能，必须申请制、规划好队列数
6. **顺序消息有代价**：吞吐下降、热点 key 问题，确认真的需要顺序再上
7. **事务消息回查逻辑要幂等、要快**：回查默认每分钟一次，查 DB 别锁表

## 八、什么时候用 MQ / 不用

**用**：耗时操作可异步化、一份数据多方消费、流量波动大需要缓冲、需要跨服务最终一致性。
**不用/慎用**：强一致的同步调用（老老实实 RPC）、简单 CRUD 单体、链路本身就要求实时返回结果。

## 九、下一步

- RocketMQ Dashboard 监控消费进度
- 结合 `nacos-config` 管理 MQ 配置多环境
- 学习 Seata（强一致分布式事务）与事务消息（最终一致）的取舍
- 消息轨迹、死信队列（DLQ）、消费限流
