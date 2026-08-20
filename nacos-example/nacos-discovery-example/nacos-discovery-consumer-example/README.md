# nacos-discovery-consumer-example

Spring Cloud Alibaba **Nacos 服务发现** 示例 —— 服务消费者端。

本项目演示了一个微服务消费者如何：

1. 将自身注册到 **Nacos** 注册中心；
2. 通过服务名（而非 IP 地址）发现并调用服务提供者 `service-provider`；
3. 使用 **RestTemplate + Spring Cloud LoadBalancer** 发起负载均衡的远程调用；
4. 使用 **OpenFeign** 声明式客户端调用远程服务，并配置 Fallback 熔断降级；
5. 通过 **Sentinel** 加载本地限流 / 熔断规则文件（flowrule.json / degraderule.json）。

> 本模块需要配合服务提供者 `nacos-discovery-provider-example`（服务名 `service-provider`）一起使用。

---

## 项目信息

| 项目 | 值 |
| --- | --- |
| 服务名 | `service-consumer` |
| 端口 | `18083` |
| 注册中心 | Nacos `127.0.0.1:8848`（账号/密码：nacos/nacos） |
| 父模块 | `nacos-discovery-example` |

## 目录结构

```
nacos-discovery-consumer-example
├── pom.xml
└── src/main
    ├── java/com/thm/cloud
    │   ├── ConsumerApplication.java        # 启动类：@EnableDiscoveryClient + @EnableFeignClients
    │   ├── TestController.java             # 演示用测试接口（RestTemplate / Feign / 服务发现）
    │   ├── configuration
    │   │   ├── RestTemplateConfiguration.java  # 两个 @LoadBalanced RestTemplate
    │   │   ├── FeignConfiguration.java         # 注册 Feign Fallback Bean
    │   │   └── UrlCleaner.java                 # Sentinel URL 清洗器（规范化资源名）
    │   └── feign
    │       ├── EchoClient.java                # Feign 声明式客户端（指向 service-provider）
    │       └── EchoClientFallback.java        # 熔断/异常时的降级实现
    └── resources
        ├── application.yml                # Nacos / Sentinel / Feign 配置
        ├── flowrule.json                  # Sentinel 限流规则
        └── degraderule.json               # Sentinel 熔断降级规则
```

## 核心说明

### 1. 启动类 `ConsumerApplication`

- `@EnableDiscoveryClient`：开启服务发现，自动注册到 Nacos；
- `@EnableFeignClients`：开启 Feign 客户端扫描；
- `@LoadBalancerClient("service-provider")`：为 `service-provider` 指定 LoadBalancer 客户端配置。

### 2. 两种远程调用方式

**方式一：RestTemplate（负载均衡）**

`RestTemplateConfiguration` 中定义了两个标注了 `@LoadBalanced` 的 `RestTemplate`，
调用时直接使用服务名 `http://service-provider/...`，由 LoadBalancer 自动完成
服务发现 + 负载均衡选择实例。

**方式二：OpenFeign（声明式 + 熔断降级）**

`EchoClient` 通过 `@FeignClient(name = "service-provider")` 声明远程接口：

| 方法 | 远程路径 | 说明 |
| --- | --- | --- |
| `echo(str)` | `GET /echo/{str}` | 回显字符串 |
| `divide(a, b)` | `GET /divide` | 除法运算（b=0 可触发异常，验证降级） |
| `divide(a)`（default 方法） | — | 默认方法内部仍调用 `divide(a, 0)` |
| `notFound()` | `GET /notFound` | 远程返回 404，验证降级 |

当远程服务不可用、超时、异常或触发熔断时，自动回退到 `EchoClientFallback`，
返回 `"xxx fallback"`，避免错误向调用方扩散。

> 注意：`EchoClient` 的 `fallback` 属性当前未在 `@FeignClient` 上配置，
> `FeignConfiguration` 只是注册了 Fallback Bean，供开启 `feign.sentinel.enabled=true`
> 后由 Sentinel 整合使用。

### 3. Sentinel 规则

`application.yml` 中配置了 Sentinel（`eager: true` 项目启动即初始化），
并从 classpath 加载两个规则文件：

**flowrule.json（限流规则，QPS）**

| 资源 | QPS 阈值 | 效果 |
| --- | --- | --- |
| `GET:http://service-provider/echo/{str}` | 1 | 超过 1 QPS 即限流 |

**degraderule.json（熔断降级规则）**

| 资源 | 策略 | 阈值 | 熔断时长 |
| --- | --- | --- | --- |
| `GET:http://service-provider/test` | 异常比例 | 50% | 30s |
| `GET:http://service-provider` | 异常比例 | 50% | 10s |
| `GET:http://service-provider/sleep` | 慢调用比例（RT） | 20ms | 30s |
| `GET:http://service-provider/divide` | 异常比例 | 50% | 30s |

`UrlCleaner` 用于将 `/echo/xxx` 这类含动态参数的 URL 归一化为 `/echo/{str}`，
使限流资源名与规则匹配（配合 `@SentinelRestTemplate` 使用，当前因 GraalVM
兼容性问题暂被注释）。

## 测试接口列表

| 接口 | 调用方式 | 说明 |
| --- | --- | --- |
| `GET /echo-rest/{str}` | RestTemplate | 调用 provider 的 `/echo/{str}` |
| `GET /index` | RestTemplate | 调用 provider 根路径 |
| `GET /test` | RestTemplate | 调用 provider `/test`（配合熔断规则） |
| `GET /sleep` | RestTemplate | 调用 provider `/sleep`（慢调用） |
| `GET /echo-feign/{str}` | Feign | 调用 provider 的 `/echo/{str}` |
| `GET /divide-feign?a=&b=` | Feign | 除法运算，b=0 触发异常降级 |
| `GET /divide-feign2?a=` | Feign | 走 default 方法的除法 |
| `GET /notFound-feign` | Feign | 调用 404 接口，验证降级 |
| `GET /services` | DiscoveryClient | 列出注册中心所有服务名 |
| `GET /services/{service}` | DiscoveryClient | 列出指定服务的实例列表 |

Actuator 端点已全部暴露（`management.endpoints.web.exposure.include: *`），
可访问 `http://localhost:18083/actuator` 查看健康状态与指标。

## 如何运行

### 前置条件

1. 启动 Nacos Server（默认 `127.0.0.1:8848`，账号密码 nacos/nacos）；
2. （可选）启动 Sentinel Dashboard（`localhost:8080`）；
3. 先启动服务提供者 `service-provider`，再启动本消费者。

### 启动 & 验证

```bash
# 编译启动
mvn spring-boot:run

# RestTemplate 调用
curl http://localhost:18083/echo-rest/hello

# Feign 调用
curl http://localhost:18083/echo-feign/hello

# Feign 降级演示（除零异常）
curl "http://localhost:18083/divide-feign?a=1&b=0"

# 查看注册中心中的服务与实例
curl http://localhost:18083/services
curl http://localhost:18083/services/service-provider
```

## 主要依赖

| 依赖 | 版本 | 用途 |
| --- | --- | --- |
| `spring-boot-starter-web` | 3.2.9 | Web 容器与 RestTemplate |
| `spring-cloud-starter-openfeign` | 4.1.3 | 声明式远程调用 |
| `spring-cloud-starter-alibaba-nacos-config` | 2023.0.3.2 | Nacos 客户端（配置中心） |

> ⚠️ 注意：`pom.xml` 当前只引入了 `nacos-config` starter，未显式引入
> `spring-cloud-starter-alibaba-nacos-discovery` 与
> `spring-cloud-starter-alibaba-sentinel`。而 `application.yml` 中配置了
> Nacos 服务发现与 Sentinel 规则。若需完整运行本示例，建议补充这两个依赖。
