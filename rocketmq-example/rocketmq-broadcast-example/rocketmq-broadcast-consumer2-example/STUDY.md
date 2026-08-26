# 子模块学习指南：rocketmq-broadcast-consumer2-example（广播消费者 2）

> 角色：广播实验三兄弟中的**接收方 2**（端口 28083）。它存在的意义就是**与 consumer1 形成对照**，
> 一起验证广播语义。代码与 consumer1 几乎 100% 相同——**差异即知识点**。

---

## 一、文件结构

```
rocketmq-broadcast-consumer2-example
├── pom.xml
└── src/main
    ├── java/.../RocketMQBroadcastConsumer2Application.java   # 与 consumer1 仅日志文案不同
    └── resources/application.yml               # 端口 28083，其余与 consumer1 相同
```

## 二、与 consumer1 的逐项对比（这就是本模块的学习内容）

| 对比项 | consumer1 | consumer2 | 说明 |
|--------|-----------|-----------|------|
| 端口 | 28084 | 28083 | 两个独立 JVM 进程 |
| spring.application.name | ...-consumer1-... | ...-consumer2-... | 服务名不同（将来注册 Nacos 用） |
| 消费逻辑 | 打印 "Consumer1 Receive" | 打印 "Consumer2 Receive" | **仅用于肉眼区分日志来源** |
| Bean 名/函数名 | consumer | consumer | 相同 → 绑定名都是 consumer-in-0 |
| destination | broadcast | broadcast | 相同：订阅同一 Topic |
| group | broadcast-consumer | broadcast-consumer | **相同：同组（关键！）** |
| messageModel | BROADCASTING | BROADCASTING | 相同：都开广播 |

**为什么代码可以完全一样？** 因为 Spring Cloud Stream 的函数式模型下，"我是谁"由**配置**决定（name/端口），
"我消费什么"由 binding 决定。复制粘贴一个消费者工程、改端口和应用名，就是企业里"新增一个消费实例"的日常操作
（生产上更多是同一个 jar 起多份，用 `-Dserver.port=xxx` 区分，无需复制代码）。

## 三、动手实验（站在 consumer2 视角）

```bash
# 1. 三个应用全启动后，本模块控制台：完整 100 条（Consumer2 Receive ... Hello RocketMQ 0~99）
#    —— 和 consumer1 各自全量 = 广播模式成立
```

**实验 A（同组集群对照）**：两个 consumer 都去掉 BROADCASTING：
本模块与 consumer1 **加起来** 100 条，且互相**不重复**——同组负载均衡，一条消息只被一个实例处理。

**实验 B（异组集群对照）**：把本模块 group 改成 `broadcast-consumer-2`（consumer1 保持原组），且都关闭广播：
本模块 100 条、consumer1 也 100 条——**不同组 = 各自全量**。这是"广播"与"多组订阅"都能实现
"多个消费者都收到"的两种途径，区别见下表。

**实验 C（扩容演练）**：保持广播模式，再复制启动一个 consumer2 实例（IDEA Copy Configuration，
`-Dserver.port=28082`）→ 新实例也会收到全量消息（广播对实例数无感）；
如果这是集群模式 → 100 条被 3 个实例瓜分。

## 四、两种"都收到全量"的方式对比（重要认知，面试可用）

| | 广播模式（本例） | 多个不同 group（集群模式） |
|---|---|---|
| 配置 | messageModel: BROADCASTING | 各消费者 group 不同 |
| 收到方 | 每个**实例** | 每个组（组内仍竞争） |
| 位点/堆积管理 | 无（本地） | 有（broker 管理，可监控可重置） |
| 失败重投 | 无 | 有（16 次退避重试） |
| 适用 | 本地缓存刷新等"实例级"动作 | 不同**服务**订阅同一事件 |

**结论：不同服务要各自收全量 → 用不同 group 的集群模式；同一服务的每个实例都要收 → 才用广播。**

## 五、收获

- 理解"复制一个消费者"只需要改 name/端口（配置即身份）
- 通过对照实验彻底掌握 同组/异组 × 广播/集群 四种组合的行为
- 会用 `-Dserver.port` 多实例启动模拟扩缩容

## 六、日常/企业怎么用

- 生产上不会真的复制三个工程：**一个 consumer 服务打成 jar，部署 N 个副本**（K8s replicas）。
  本例拆成两个 module 只是为了教学时同时观察两份日志
- 想让新服务订阅已有事件流：copy 一份消费端配置、改 group 名、写自己的 Consumer Bean 即可——
  这就是 MQ"发布订阅解耦"的红利：**上游零改动**

## 七、注意什么

1. 别把"广播模式"当成"多个服务都要收到"的默认答案——优先考虑不同 group（有位点管理、有重试）
2. 本模块与 consumer1 同 group 且订阅一致是**前提**；若只改一处 destination/tag 会造成订阅关系不一致事故
3. 端口、应用名是唯一区分两实例的标识，日志排查时先看 `spring.application.name`
4. 教学示例把端口写死在各自 yml；生产用环境变量 `PORT`/注册中心管理

## 八、进阶练习

- [ ] 用同一个 consumer1 工程 + `-Dserver.port=28082 -Dspring.application.name=...consumer3` 再起一个实例，体会"多副本"才是常态
- [ ] 四种组合矩阵（同/异组 × 广播/集群）各跑一遍，用表格记录每个实例收到的条数，形成自己的实验报告
