# 子模块学习指南：rocketmq-broadcast-producer-example（广播生产者）

> 角色：广播实验三兄弟中的**发送方**（端口 28085）。演示用 `StreamBridge` 在启动时主动发 100 条消息。
> 必须与 consumer1(28084)、consumer2(28083) 一起启动才能看到广播效果，先看父目录 `../STUDY.md` 的整体架构。

---

## 一、文件结构

```
rocketmq-broadcast-producer-example
├── pom.xml                                     # starter-stream-rocketmq + web + actuator
└── src/main
    ├── java/.../RocketMQBroadcastProducerApplication.java   # StreamBridge 发消息
    └── resources/application.yml               # 只配了输出绑定，没有消费
```

## 二、代码精读

### 1. 发送方式：StreamBridge（企业里的主流写法）

```java
@Autowired
private StreamBridge streamBridge;              // 桥接器：业务代码任意位置想发就发

@Bean
public ApplicationRunner producer() {           // 应用启动完成后执行一次
    return args -> {
        for (int i = 0; i < 100; i++) {
            Map<String, Object> headers = new HashMap<>();
            headers.put(MessageConst.PROPERTY_KEYS, "KEY" + i);          // 消息唯一 KEY
            headers.put(MessageConst.PROPERTY_ORIGIN_MESSAGE_ID, i);     // 业务序号 header
            Message<SimpleMsg> msg =
                new GenericMessage<>(new SimpleMsg("Hello RocketMQ " + i), headers);
            streamBridge.send("producer-out-0", msg);                    // 按"绑定名"发送
        }
    };
}
```

**三个知识点**：
1. **`streamBridge.send("producer-out-0", msg)`**：第一个参数是**绑定名**（不是 Topic 名！），框架按绑定找到 `destination: broadcast` 再发出去。Controller/Service 里随时可注入 StreamBridge 发消息——这是生产上最常见的发送方式（对比 comprehensive 模块的 Supplier 定时器写法）
2. **`GenericMessage<>(payload, headers)`**：payload 会被 JSON 序列化；headers 是元数据（KEY、TAG、自定义属性都放这）
3. **`PROPERTY_KEYS`**：消息 KEY，用于控制台按 key 查询、消费幂等去重的依据。生产上 KEY = 业务唯一键（订单号、事件 ID）

### 2. application.yml —— 纯生产者的最小配置

```yaml
spring:
  cloud:
    stream:
      rocketmq:
        binder:
          name-server: localhost:9876           # RocketMQ 地址
        bindings:
          producer-out-0:
            producer:
              group: output_1                   # RocketMQ 要求 producer 必须有 group
      bindings:
        producer-out-0:
          destination: broadcast                # 绑定名 → Topic: broadcast
```
注意：**没有 `function.definition`、没有 Consumer** —— StreamBridge 发送不依赖函数定义，只要 `bindings` 里有输出绑定就行。这就是它比 Supplier 灵活的原因。

## 三、动手实验

```bash
# 1. 建 topic: broadcast（命令见父级 STUDY.md）
# 2. 先启动两个 consumer，最后启动本 producer
# 3. 本模块自己的控制台：只有发送日志，没有任何消费输出（它不订阅）
```
**实验 A**：给 producer 加个 Controller，改成接口触发发送（最贴近生产）：
```java
@RestController
static class SendController {
    @Autowired StreamBridge streamBridge;
    @GetMapping("/send")
    public boolean send(@RequestParam String msg) {
        return streamBridge.send("producer-out-0",
            new GenericMessage<>(new SimpleMsg(msg), Map.of()));
    }
}
```
再 `curl http://localhost:28085/send?msg=hi`，两个 consumer 立即各打印一条。

## 四、收获

- 掌握 StreamBridge 主动发送（`send(绑定名, 消息)`）——之后所有模块的发送全是这个套路
- 理解绑定名与 Topic 的映射关系
- 知道消息 KEY 的用途

## 五、日常/企业怎么用

- 发送方就是业务方：订单服务落库后 `streamBridge.send("order-out-0", new GenericMessage<>(orderEvent))`
- **生产必须处理发送失败**：send 返回 boolean，false 时记表重发（StreamBridge 本身不重试）；重要场景用RocketMQ 同步发送确认机制或本地消息表兜底
- 发送尽量在**事务提交后**执行（或直接用事务消息，见 tx 模块），避免"库没提交、消息先出去"

## 六、注意什么

1. `send()` 的第一个参数写错绑定名 → 运行时临时创建绑定或抛异常（取决于版本），务必与 yml 一致
2. header 里**别放不可序列化对象**（比如实体 Bean），只放基本类型/String
3. 循环 100 次同步 send 是演示；生产高并发发送无需自己加锁，producer 客户端线程安全
4. 发送端无法感知"有没有人消费"——广播与否完全是**消费端配置**决定的，生产者对此无感

## 七、进阶练习

- [ ] 加 Controller 版发送接口（上面代码）
- [ ] 发送时带上 `MessageConst.PROPERTY_TAGS` header，然后只让 consumer 订阅某个 Tag（结合 sql 模块知识）
