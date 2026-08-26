# 模块学习指南：rocketmq-broadcast-example（广播消费 · 3 个子模块一组实验）

> 角色：验证 MQ 最重要的消费模型差异——**集群模式（CLUSTERING，默认）vs 广播模式（BROADCASTING）**。
> 本模块 3 个子应用必须一起跑：producer 发 100 条，consumer1、consumer2 同时收。

## 一、架构

```
                        Topic: broadcast (100条消息)
                     ┌──────────┴──────────┐
          集群模式(默认)                     广播模式(本例)
   consumer1 ┐ 同 group 竞争,合起来          consumer1(28084) 全部 100 条 ✔
   consumer2 ┘ 各拿一部分                    consumer2(28083) 全部 100 条 ✔
                                             （两个 consumer 同 group=broadcast-consumer
                                               但配置了 messageModel: BROADCASTING）
```

## 二、子模块与关键差异点

| 子模块 | 端口 | 说明 |
|--------|------|------|
| rocketmq-broadcast-producer-example | 28085 | StreamBridge 循环发 100 条 SimpleMsg |
| rocketmq-broadcast-consumer1-example | 28084 | `Consumer<Message<SimpleMsg>>` Bean 收消息 |
| rocketmq-broadcast-consumer2-example | 28083 | 同上，代码几乎一样，只是日志里叫 Consumer2 |

**唯一的模式开关就在消费端 yml（两行决定一切）**：
```yaml
rocketmq:
  bindings:
    consumer-in-0:
      consumer:
        messageModel: BROADCASTING    # 广播：每个消费者实例都收全量；删掉这行=CLUSTERING 集群瓜分
```

## 三、逐文件精读

### 1. 生产者：StreamBridge —— 生产最常用的发送方式
```java
@Autowired
private StreamBridge streamBridge;                 // 桥：任何业务代码里想发就发

Message<SimpleMsg> msg = new GenericMessage<>(new SimpleMsg("Hello RocketMQ " + i), headers);
streamBridge.send("producer-out-0", msg);          // 按绑定名发送 → Topic: broadcast
```
对比 comprehensive 模块的 Supplier：**StreamBridge 不需要定时器，写在 Controller/Service 里，请求来了才发**——这才是企业里的主流用法。

### 2. 消息头 Headers 的两个属性
```java
headers.put(MessageConst.PROPERTY_KEYS, "KEY" + i);          // 消息 KEY：唯一标识，排查/幂等的关键
headers.put(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, i);     // 自定义业务头：示例中当序号用
```
KEY 在 RocketMQ 控制台按 key 查消息轨迹；生产上 KEY=业务唯一键（订单号）。

### 3. 消费者：函数式 Consumer Bean
```java
@Bean
public Consumer<Message<SimpleMsg>> consumer() {
    return msg -> log.info(Thread.currentThread().getName() + " Consumer1 Receive: " + msg.getPayload().getMsg());
}
```
注意参数是 `Message<SimpleMsg>` 而非裸 `SimpleMsg`——包一层才能拿到 headers。

## 四、动手实验（本模块精华）

```bash
# 1. 建 topic broadcast，启动 RocketMQ
# 2. 先后启动 consumer1、consumer2，最后启动 producer
```
**实验 A（默认集群模式）**：把两个消费者 yml 里的 `messageModel: BROADCASTING` 都删掉再跑：
- 两个消费者**合计**收到 100 条（比如 51+49），同一个 group 内瓜分 → 这就是集群模式/负载均衡

**实验 B（广播模式，原样跑）**：
- 每个消费者各收到**完整 100 条** → 广播模式

**实验 C（理解 group 命名）**：把 consumer2 的 group 改成 `broadcast-consumer-2`（不同组）：
- 广播模式下没区别（广播无视 group 瓜分）
- 但集群模式下不同组 = 各自全量 100 条！（回忆总览里的"Group 黄金法则"）

**实验 D（扩展位点知识）**：广播模式下消费者**不提交消费位点到 broker**，重启后会不会重收？
跑一下：停掉 consumer1 再启动 → 又收一遍全量（广播模式消费位点保存在本地，offset 不在 broker）。

## 五、收获

- 会用 StreamBridge 主动发消息（生产主流写法）
- 彻底掌握 集群 vs 广播 的行为差异与配置开关（`messageModel`）
- 理解 group 竞争关系与广播模式的关系
- 知道广播模式位点在本地、无法用控制台管理堆积

## 六、日常/企业怎么用

**广播的三大真实场景（背下来）**：
1. **本地缓存同步**：商品信息缓存在每台机器 JVM 里，数据变更时广播一条"刷新 XX 缓存"，所有实例都收到、各自刷新
2. **配置/规则推送**：风控规则、黑白名单更新推给全部节点
3. **WebSocket/长连接推送**：用户连在任意节点上，广播保证连到哪个节点都能收到

其余 90% 场景（订单处理、日志）都用**默认集群模式**——扩容自动分摊流量才是常态需求。

## 七、注意什么

1. 广播模式下**没有重投/堆积管理**：消费失败就丢了（broker 不记位点），重要业务别用广播
2. 广播模式不保证消费顺序与集群模式一致
3. 集群模式下同 group 的实例**订阅关系必须一致**（订阅同一套 Topic/Tag），否则订阅互相覆盖导致丢消息（RocketMQ 经典事故）
4. 想让"每个服务"都收全量 → 给每个服务**不同的 group**（集群模式即可），不必非用广播；广播的语义是"每个**实例**都收"

## 八、进阶练习

- [ ] 把 producer 改造成 Controller：`POST /send?msg=xxx` 触发发送（最贴近生产）
- [ ] 用 RocketMQ Dashboard 对比两种模式下消费位点（Offset）的展示差异
