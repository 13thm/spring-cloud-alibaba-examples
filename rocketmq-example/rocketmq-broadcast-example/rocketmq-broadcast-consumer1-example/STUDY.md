# 子模块学习指南：rocketmq-broadcast-consumer1-example（广播消费者 1）

> 角色：广播实验三兄弟中的**接收方 1**（端口 28084）。与 consumer2 配合验证"广播模式：每个实例都收到全量消息"。
> 整体架构与实验清单见父目录 `../STUDY.md`。

---

## 一、文件结构

```
rocketmq-broadcast-consumer1-example
├── pom.xml
└── src/main
    ├── java/.../RocketMQBroadcastConsumer1Application.java   # Consumer Bean
    └── resources/application.yml               # messageModel: BROADCASTING ← 关键开关
```

## 二、代码精读

### 1. 消费方式：函数式 Consumer Bean（被动推送）

```java
@Bean
public Consumer<Message<SimpleMsg>> consumer() {     // Bean 名 = 函数名 = 绑定名前缀
    return msg -> {
        log.info(Thread.currentThread().getName() + " Consumer1 Receive New Messages: "
            + msg.getPayload().getMsg());
    };
}
```
要点：
- `Consumer<Message<SimpleMsg>>`：**包一层 `Message<>` 才能拿到 headers**；裸 `Consumer<SimpleMsg>` 只能拿 payload
- 这里的 Consumer 是 `java.util.function.Consumer`（不是 RocketMQ 的），框架自动把它接到 MQ
- 方法体里抛异常 = 消费失败（触发重试）；正常返回 = ACK 确认

### 2. application.yml —— 逐行拆解（本模块灵魂）

```yaml
spring:
  application:
    name: rocketmq-broadcast-consumer1-example
  cloud:
    stream:
      function:
        definition: consumer;                  # ① 注册名为 consumer 的函数 → 自动绑定 consumer-in-0
      rocketmq:
        binder:
          name-server: localhost:9876
        bindings:
          consumer-in-0:
            consumer:
              messageModel: BROADCASTING       # ② 广播模式开关（RocketMQ 专属配置）
      bindings:
        consumer-in-0:
          destination: broadcast               # ③ 订阅 Topic: broadcast
          group: broadcast-consumer            # ④ 消费组（与 consumer2 相同！）
```

**配置三层结构要记牢**：
| 层 | 位置 | 作用 |
|----|------|------|
| ① | `function.definition` | 声明哪些函数参与绑定（分号分隔多个） |
| ② | `rocketmq.bindings.<name>.consumer` | RocketMQ 专属增强（messageModel、tags、push 等） |
| ③④ | `bindings.<name>` | 通用绑定：接哪个 Topic、哪个 group |

### 3. 广播模式语义（结合 consumer2 理解）

consumer1 和 consumer2 用**同一个 group（broadcast-consumer）**：
- 集群模式（默认）：同 group 瓜分 100 条 → 各约 50 条
- **广播模式（本例）**：同 group 也不瓜分，**各自收到全部 100 条** ← 就是 `messageModel: BROADCASTING` 一行造成的效果差异

## 三、动手实验（必做，对照父目录实验 A/B/C）

```bash
# 1. 启动本模块 + consumer2 + producer
# 2. 本模块控制台数一下：应该是完整 100 条（Consumer1 Receive ... Hello RocketMQ 0~99）
# 3. 删掉 yml 里 messageModel: BROADCASTING（两个 consumer 都删）重启 → 变成约 50 条（集群瓜分）
# 4. 只删本模块的 BROADCASTING（consumer2 保留广播）→ 本模块 50 条 + consumer2 100 条
#    （模式是每个消费者实例各自决定的！）
```

**重启实验（广播位点的坑）**：广播模式下停掉本模块再启动 → **重新收到全量 100 条**？
实际不会——RocketMQ 4.x 广播消费位点存**本地文件**，同机重启会续位点；换机器/清目录就会重收。
这就是广播模式"位点不可靠"的直观体验。

## 四、收获

- 掌握消费端最小配置四件套：definition / destination / group / messageModel
- 理解 `Message<T>` 包装与裸 payload 的区别
- 彻底搞懂广播 vs 集群：**同 group 同 Topic，一行配置改变行为**
- 亲身体验广播模式位点在本地的问题

## 五、日常/企业怎么用

广播消费者的典型画像（背下来）：
1. **本地缓存刷新**：每台机器 JVM 里有商品缓存 → 收到广播"商品 123 变更" → 各自 `cache.evict(123)`
2. **规则/配置热更新**：风控规则推送到所有节点内存
3. **节点任务同步**：通知所有实例立刻刷新权限、清理临时文件

判断口诀：**"每台机器都要做一次"的事 → 广播；"这件事只需一个实例做" → 集群**。

## 六、注意什么

1. **消费逻辑必须幂等**：广播重启可能重收（见实验），集群模式重平衡也会重复投递
2. 广播模式 **broker 不管理位点**：无堆积监控、无重投保障，消费失败≈丢失，重要流程禁用
3. `definition: consumer;` 末尾分号或拼写错误 → 绑定不生效，日志未必显眼，第一次跑先确认 `consumer-in-0` 出现在启动日志
4. 同 group 的所有实例 `destination`+`tags` 订阅必须完全一致，否则订阅关系错乱（RocketMQ 经典丢消息事故）
5. 广播模式下 group 名的"竞争"含义失效，但仍要命名规范（用于区分业务用途）

## 七、进阶练习

- [ ] 模拟缓存刷新：模块里加 `Map<String,String> cache`，收到消息 evict 对应 key，验证两实例都执行
- [ ] 把消费方法里故意抛 RuntimeException，观察广播模式下的重试/丢弃行为（对比集群模式的重试 16 次）
