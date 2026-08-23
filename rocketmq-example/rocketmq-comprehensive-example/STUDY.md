# 模块学习指南：rocketmq-comprehensive-example（函数式编程模型 · 打地基）

> 角色：**第一个必学模块**。用最纯粹的 Spring Cloud Stream 函数式写法演示
> Supplier（生产）→ Function（加工）→ Consumer（消费）的完整管道，是后面所有模块的地基。

---

## 一、架构：一条流式管道，两个 Topic

```
 Supplier<Flux<User>>              Function<Flux<User>,Flux<User>>            Consumer<User>
 每 2 秒造一个 User                读出来把 id 平方、改名、改爱好             打印结果
      │                                 │                                      ▲
      ▼                                 ▼                                      │
 producer-out-0 ──▶ Topic: num ──▶ processor-in-0  processor-out-0 ──▶ Topic: square ──▶ consumer-in-0
                    (第1站)                                          (第2站)
```

一个应用里同时扮演生产者、加工者、消费者三种角色（实际企业中通常拆在不同服务里）。

## 二、逐文件精读

### 1. RocketMQComprehensiveApplication.java —— 函数式三件套

```java
@Bean
public Supplier<Flux<User>> producer() {        // Supplier：只有出参 → 发消息（定时触发）
    return () -> Flux.interval(Duration.ofSeconds(2)).map(id -> { /* 每2秒产一个User */ });
}

@Bean
public Function<Flux<User>, Flux<User>> processor() {   // Function：有入有出 → 收一条、加工、再发一条
    return flux -> flux.map(user -> { user.setId(平方); user.setName("not freeman"); ... });
}

@Bean
public Consumer<User> consumer() {              // Consumer：只有入参 → 收消息（终点）
    return num -> log.info(num.toString());
}
```

**核心认知（面试高频）**：

| 函数 | 方向 | Binding 自动生成 | 触发方式 |
|------|------|------------------|----------|
| `Supplier<T>` | 输出 | `producer-out-0` | 定时轮询（默认 1s 调一次，返回非空就发） |
| `Consumer<T>` | 输入 | `consumer-in-0` | MQ 推过来就触发（push 模式） |
| `Function<T,R>` | 输入+输出 | `xxx-in-0` / `xxx-out-0` | 收到消息→执行→返回值自动发到 out |

`Flux<T>` 是 Reactor 响应式流，支持批量/流式处理；用普通 `T` 也完全可以（一次一条）。

### 2. application.yml —— Binding 配置全解（本模块灵魂）

```yaml
spring:
  cloud:
    stream:
      function:
        definition: producer;consumer;processor   # ① 注册函数（多个用分号），不写=不生效
      rocketmq:
        binder:
          name-server: 127.0.0.1:9876             # ② RocketMQ 地址（SCA 特有段）
        bindings:
          producer-out-0:
            producer:
              group: output_1                     # ③ RocketMQ 要求 producer 必须有 group
      bindings:                                    # ④ 通用段（换 Kafka 这些不用变）
        producer-out-0:
          destination: num                         #    out 绑定 → Topic num
        processor-in-0:
          destination: num                         #    in 绑定 ← Topic num
          group: processor_group                   #    消费组
        processor-out-0:
          destination: square                      #    加工后发往 Topic square
        consumer-in-0:
          destination: square
          group: consumer_group
```

**记忆口诀**：
- `bindings.<函数名>-in/out-N.destination` = 这个口子接哪个 Topic
- `in` 必须配 `group`（没有 group 的消费在 RocketMQ 里无法管理位点）
- `rocketmq.bindings.*` 段是 RocketMQ 专属增强；`bindings.*` 段是 Stream 通用配置

### 3. User.java —— 强类型消息体
含嵌套 Map（meta），演示复杂对象 JSON 序列化照样收发。

## 三、动手实验

```bash
# 前置：创建 topic（num、square）或临时开 broker 自动建 topic
# 启动 RocketMQComprehensiveApplication，观察日志：
# 1. 每 2 秒 supplier 产一条 id=0,1,2,3...
# 2. processor 打印收到的是 square 后的 id=0,1,4,9...
# 3. consumer 打印 User{id='9', name='not freeman', meta={hobbies=[programming], age=21}}
```
**实验**：把 `processor` 从 `function.definition` 里删掉 → 消息停在 `num` Topic 无人加工；重新加回 → `processor_group` 会从上次位点继续消费（验证**位点按 group 持久化**）。

## 四、收获

- 会写函数式三件套并配 Binding，理解 `definition` 与 `-in/out-N` 的对应关系
- 理解 Supplier 定时轮询触发 vs Consumer 被动推送触发
- 理解 Function = 收+发两根管道的"中间站"（ETL/加工链路的模型）
- 理解 group 位点持久化：消费组重启从上次位置继续，不丢不重（除非换 group）

## 五、日常/企业怎么用

- **典型分工**：订单服务只声明 `Supplier`（或用 StreamBridge）发"订单事件"；风控/积分/推送各服务声明自己的 `Consumer` 订阅。本模块的 processor 形态常见于数据清洗/风控加工链
- 生产者一般不写 Supplier（定时器很少见），更多用 **StreamBridge 在业务代码里主动发**（见其他模块）
- Topic 命名：`{业务域}.{事件名}` 如 `order.created`；group：`{服务名}-{用途}-group`

## 六、注意什么

1. `function.definition` 忘写/拼错 → 函数不绑定，消息发不出收不到，日志还可能不报错，**新头号坑**
2. 同一个函数名如果与 Spring 内置 Bean 冲突（如 `processor`），注意作用域
3. 一次发一条用 `T`，要高吞吐批量才考虑 `Flux<T>`（新手先别碰响应式）
4. consumer group 不要和生产 group 混用概念：producer 的 group 只是发消息者的标识，位点管理只看消费组

## 七、进阶练习

- [ ] 加一个 `Consumer<User> audit()` 同时订阅 `num` Topic（不同 group）验证"一份数据多方消费"
- [ ] 把 Supplier 换成 Controller 里用 StreamBridge 发（更贴近生产写法）
