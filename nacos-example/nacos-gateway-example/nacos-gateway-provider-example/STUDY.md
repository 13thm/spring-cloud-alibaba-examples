# 模块学习指南：nacos-gateway-provider-example（服务提供者）

> 角色：**业务服务**。它是最普通的 Spring Boot Web 应用，唯一的"微服务特征"是把自己注册到 Nacos。

---

## 一、本模块文件结构

```
nacos-gateway-provider-example
├── pom.xml                                  # 依赖：web + nacos-discovery + actuator
└── src/main
    ├── java/com/alibaba/cloud/examples
    │   ├── ProviderApplication.java         # 启动类 + @EnableDiscoveryClient
    │   └── EchoController.java              # 两个测试接口
    └── resources/application.yml            # 服务名、Nacos 地址、端口
```

## 二、逐文件精读

### 1. pom.xml —— 只多了一个"注册"依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```
理解：引入它之后，应用启动时会自动：
1. 向 Nacos **注册**自己（服务名 + IP:Port）
2. 定期发送**心跳**（默认 5 秒一次）证明自己活着
3. 被 Nacos 主动**剔除**（心跳停 15 秒后）时下线

**不需要写任何注册代码**，全部自动完成——这是 Spring Cloud "约定优于配置"的典型体现。

### 2. application.yml —— 三个关键配置

```yaml
spring:
  application:
    name: service-gateway-provider   # ① 服务名 = 服务在 Nacos 里的唯一标识
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848  # ② Nacos 注册中心地址
        username: nacos              # ③ 认证（生产要改）
server:
  port: 18086
```

**重点：`spring.application.name` 是整个微服务体系的"身份证"**。
网关路由里的 `lb://service-gateway-provider`、将来 OpenFeign 的 `@FeignClient("service-gateway-provider")`，全都靠这个名字找到它。改名字 = 网关路由和所有调用方都要跟着改。

### 3. ProviderApplication.java —— @EnableDiscoveryClient

```java
@SpringBootApplication
@EnableDiscoveryClient   # 开启服务注册发现客户端
public class ProviderApplication { ... }
```
知识点：在较新的 Spring Cloud 版本里，`@EnableDiscoveryClient` 其实**可以省略**（只要 classpath 有 nacos-discovery 依赖就会自动注册）。写上是为了表达明确意图，面试常问。

### 4. EchoController.java —— 最普通的 REST 接口

```java
@GetMapping("/echo/{string}")   // 返回 "hello Nacos Discovery xxx"
@GetMapping("/divide")          // a / b，注意：b=0 会抛 ArithmeticException（可用来观察网关的异常处理）
```
**它完全不知道网关的存在**。这正是微服务的解耦思想：
- 被网关转发调用时，Controller 收到的路径是 `/echo/hi`（前缀已被 StripPrefix 剥掉）
- 对它来说，来自网关的请求和来自浏览器的请求没有任何区别

## 三、动手验证

```bash
# 1. 启动 Nacos，再启动本服务
# 2. 直连自己（证明它就是个普通 Web 服务）
curl http://localhost:18086/echo/hi
# → hello Nacos Discovery hi

# 3. 查看 Nacos 控制台 (http://localhost:8848/nacos)
#    服务管理 → 服务列表 → 能看到 service-gateway-provider，实例 IP:18086，健康
```

## 四、学完本模块你应收获

1. 理解 **"服务注册" 是什么**：把 `服务名 → IP:Port` 写进 Nacos，供别人查找
2. 理解 **心跳与健康检查**：实例挂了，Nacos 怎么知道、多久知道
3. 理解 **服务名解耦**：调用方只记服务名，IP 变化、扩容都不影响
4. 知道 Provider 本身可以完全不懂网关（职责分离）

## 五、日常怎么用 / 企业怎么用

**日常**：每写一个新微服务，标配就是本模块这套：`web + nacos-discovery + actuator`，配好 `spring.application.name`。这就是"把服务挂到通讯录上"。

**企业**：
- 服务按业务域命名：`user-service`、`order-service`、`pay-service`
- 一个服务通常部署多实例（2 个起）保证高可用
- 生产 Nacos 是集群（3 节点起）+ 独立部署，地址、账号通过环境变量/配置注入，不写死在 yml
- `actuator/health` 会被 K8s/Nacos 用作健康检查探针

## 六、注意什么 / 什么时候这么用

| 场景 | 做法 |
|------|------|
| 要被网关/其他服务调用 | 必须注册 Nacos，且服务名全局唯一、稳定 |
| 纯后台任务（定时 Job）不需要被调用 | 可以不注册，减少暴露面 |
| 修改了端口 | 重启即自动更新注册信息，无需通知网关 |
| 优雅下线 | 先在 Nacos 控制台把实例权重调 0 / 标记下线，流量切走再停进程（直接 kill 有几秒 503 窗口） |
| 安全 | Provider 端口在内网不应暴露公网；生产上业务服务只允许网关集群访问 |
| b=0 的 divide | 网关默认返回 500 JSON；生产要全局异常处理返回统一错误体 |
