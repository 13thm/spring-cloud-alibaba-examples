# 模块学习指南：rocketmq-pollable-consume-example（轮询/主动拉取消费）

> 角色：演示 **Pull 消费**：不用 @Bean Consumer 被动等推送，而是自己起线程按自己的节奏 `poll()` 拉消息。

## 一、Push vs Poll（先建立对比）

| | Push（前面所有模块） | Pollable（本模块） |
|---|---|---|
| 触发方 | MQ 推给你（底层其实也是长轮询） | 你自己去拉 |
| 代码形态 | `@Bean Consumer<T>` | `PollableMessageSource.poll()` |
| 消费节奏 | Broker/客户端定 | **自己完全掌控**（何时拉、拉多少、拉完干嘛） |
| 典型场景 | 实时业务处理 | 批处理、限流消费、批处理同步任务 |

## 二、逐文件精读

### 1. application.yml —— 声明 pollable source
```yaml
spring:
  cloud:
    stream:
      pollable-source: pollable        # ← 关键：声明一个可轮询的输入源（不是 function.definition！）
      bindings:
        pollable-in-0:                 # 自动生成 pollable-in-0 绑定
          destination: pollable
        producer-out-0:
          destination: pollable
```
注意：**消费侧没有 group**（pollable 源由自己管理拉取），这和 Consumer Bean 不同。

### 2. 主类 —— 手动 poll 循环
```java
public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(...);
    PollableMessageSource destIn = context.getBean(PollableMessageSource.class);
    new Thread(() -> {
        while (true) {
            if (!destIn.poll(m -> {                       // poll 一次拿一条，有消息返回 true
                SimpleMsg payload = (SimpleMsg) m.getPayload();
                System.out.println(payload.getMsg());
            }, new ParameterizedTypeReference<SimpleMsg>() {})) {
                Thread.sleep(1000);                       // 没消息歇 1 秒再拉（自己控制节奏）
            }
        }
    }).start();
}
```
要点：
- `poll(handler, type)` 返回 boolean：**true=拿到了并已处理，false=当前无消息**
- 处理逻辑写在内置 lambda 里；抛异常=处理失败（会 NACK，消息不确认）
- 自己 while(true) 控制节奏：没有就 sleep——这就是**消费限流**的雏形

## 三、动手实验

```bash
# 1. 建 topic: pollable，启动应用
# 2. 观察：producer 先瞬间发完 100 条，poll 线程"匀速"逐条打印（无 sleep 处理很快时几乎是连续的）
# 3. 在 poll 的 lambda 里加 Thread.sleep(100) → 变成 10 条/秒匀速消费 → 理解"消费节奏自己定"
# 4. 断点观察：poll 返回 false 时线程在 sleep —— 拉空不报错
```

## 四、收获

- 知道 Spring Cloud Stream 除了 Consumer Bean 还有 PollableMessageSource 这条路
- 理解 Push/Pull 两种消费模型的取舍
- 掌握"自己控制消费速度"的写法（限流消费/削峰消费端的核心思想）

## 五、日常/企业怎么用

- **同步型批处理**：每小时拉一批用户变更同步到 ES/数仓，不要求实时
- **消费端自我保护**：下游 DB 压力大时主动放慢拉取（Push 模式做不到优雅降速）
- **搭积木式批处理**：攒 500 条批量入库，拉取模型更好实现（push 也能攒，但节奏控制别扭）

大多数业务用默认 Push 就够了；**Pull 用于"我要控制节奏"的场景**。

## 六、注意什么

1. pollable source **没有消费组管理**，位点语义与 Consumer Bean 不同，别混用两种方式消费同一个 binding
2. 示例里自己 new Thread 不是最佳实践，生产建议用线程池/ScheduledExecutor
3. 无消息时记得退避（sleep），死循环空转会打爆 CPU 和 Broker
4. batch 拉取（一次多条）在 Spring Cloud Stream 里支持有限，重度批量场景可直接用 RocketMQ 原生 `DefaultLitePullConsumer`

## 七、进阶练习

- [ ] 改造成 ScheduledExecutorService 每 500ms poll 一次
- [ ] 实现"攒 10 条批量打印"的迷你批处理
