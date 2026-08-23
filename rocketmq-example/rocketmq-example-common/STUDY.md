# 模块学习指南：rocketmq-example-common（公共模块）

> 角色：**公共依赖库**。没有任何启动类，只放所有示例共用的消息体 `SimpleMsg`。

## 一、内容

```
rocketmq-example-common
├── pom.xml
└── src/main/java/com/alibaba/cloud/examples/common/SimpleMsg.java
```

`SimpleMsg` 就一个 `String msg` 字段 + 无参构造 + getter/setter。

## 二、知识点

1. **消息体必须是可序列化的 POJO**：本套示例配置了 `content-type: application/json`，发送时由 Spring Cloud Stream 自动把 `SimpleMsg` 序列化为 JSON，消费时再反序列化回来。所以必须有**无参构造器和 getter/setter**（Jackson 需要）。
2. **为什么要独立 common 模块**：生产者写消息结构、消费者解析消息结构，两边必须用同一个类才能正确序列化/反序列化。把它抽成独立 jar，双方都依赖，**保证消息契约一致**——这正是企业里"API 模块 / DTO 模块"的做法。
3. 注意 `application.yml` 在这个模块里**不会被加载**（它不是启动应用），纯属占位。

## 三、日常/企业怎么做

- 消息体放独立 `xxx-api` 或 `xxx-common` 模块，随接口文档一起版本化管理
- 消息结构变更要**向后兼容**（只加字段不删字段），否则老消费者反序列化崩掉
- 重要消息建议带上：`eventId`（唯一 ID，幂等用）、`timestamp`、`version`、`traceId`
- 生产上别用 `Map` 传业务数据（示例综合模块用了 meta Map 只是演示），用强类型 DTO

## 四、注意什么

- 给消息体显式声明 `serialVersionUID`（虽然走 JSON 时用不到，但避免换序列化方式时踩坑）
- 生产者与消费者的此类**包名必须一致**（JSON 按 类名 反序列化时有影响，RocketMQ 默认映射按类型匹配）
