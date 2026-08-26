# 模块学习指南：rocketmq-sql-consume-example（SQL92 消息过滤）

> 角色：演示消费端**在 Broker 上按条件过滤**，只拉自己关心的消息子集。一个应用自产自销。

## 一、原理一句话

> 生产者把业务属性放进消息 header（`color=red1`、`price=4`）；
> 消费者订阅时声明 SQL92 表达式，**Broker 只把匹配的消息投递给该消费者**。
> 过滤发生在 Broker 端，不匹配的消息根本不会走网络——这是和"收下来自己 if 判断"的本质区别。

## 二、关键配置与代码

### 1. 消费端订阅表达式（yml 里唯一的新东西）
```yaml
rocketmq:
  bindings:
    consumer-in-0:
      consumer:
        # tag: {@code tag1||tag2||tag3 }; sql: {@code 'color'='blue' AND 'price'>100 }
        subscription: sql:(color in ('red1', 'red2', 'red4') and price>3)
```
两种过滤语法：
- **Tag 过滤**（简单）：`subscription: 'TagA || TagC || TagD'`，只能等值匹配 OR
- **SQL92 过滤**（强大）：`sql:(color in (...) and price>3)`，支持 =, >, <, BETWEEN, IN, AND, OR, IS NULL

### 2. 生产端塞业务属性进 header
```java
headers.put("color", color[i % 5]);     // red1..red5 循环
headers.put("price", price[i % 5]);     // 1..5 循环
```
100 条消息里，满足 `color in (red1,red2,red4) and price>3` 的只有约 60 条（red4+price4、red4+price5 等组合）。

### 3. 消费端读 header
```java
msg.getHeaders().get("color") / get("price")
```

## 三、动手实验

```bash
# 1. 建 topic: sql
# 2. 关键前置！broker 必须开启属性过滤（默认关闭，直接跑会报错）
#    在 broker 配置文件加: enablePropertyFilter=true，重启 broker：
#    sh bin/mqbroker -n localhost:9876 -c conf/broker.conf
# 3. 启动应用，观察日志：只有 COLOR 为 red1/red2/red4 且 PRICE>3 的消息被打印（数一数是不是 ~60 条）
# 4. 改成 tag 过滤试试：subscription: 'TagA || TagC'（需要生产端加 PROPERTY_TAGS header）
```

**经典报错对照**：`MQClientException: The broker does not support consumer to filter message by SQL92`
→ 就是没开 `enablePropertyFilter=true`，90% 的人第一次跑都栽在这。

## 四、收获

- 掌握 Tag 与 SQL92 两种服务端过滤写法
- 理解"**Broker 端过滤**省带宽"：不匹配的消息不出 Broker
- 知道 header（用户属性）才是过滤的依据，payload 不能参与过滤

## 五、日常/企业怎么用

- **一个大 Topic 多业务订阅**：交易事件 Topic，营销服务只订 `sql:(eventType='pay' and amount>1000)`，审计服务订全部
- **Tag 做一级路由**（最常用）：订单 Topic 下 `created/paid/shipped` 各打 Tag，下游按需订阅
- 减少无用消费：过滤比"全收再丢弃"省机器和网络

## 六、注意什么

1. **SQL 过滤需要 broker 开 `enablePropertyFilter=true`**（Tag 过滤不需要）
2. 过滤字段必须是**消息属性**，不能是消息体内容；数值比较时注意类型
3. SQL 表达式写错只有订阅时报错，运行期悄悄收不到消息——先小流量验证
4. 复杂过滤逻辑别全堆 Broker，难维护；简单路由用 Tag，复杂条件也可考虑消费者端过滤兜底

## 七、进阶练习

- [ ] 改造为 Tag 过滤：生产端 `headers.put(MessageConst.PROPERTY_TAGS, ...)`，消费端 `subscription: 'TagA || TagB'`
- [ ] 故意写一个错误表达式（如 `price>abc`），观察启动报错行为
