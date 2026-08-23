# 模块学习指南：nacos-gateway-discovery-example（网关）

> 角色：**API 网关**。所有外部请求的统一入口，负责"找到服务并转发"。

---

## 一、本模块文件结构

```
nacos-gateway-discovery-example
├── pom.xml                                  # 关键：gateway + nacos-discovery + loadbalancer
└── src/main
    ├── java/com/alibaba/cloud/examples
    │   └── GatewayApplication.java          # 启动类 + @LoadBalancerClients
    └── resources/application.yml            # 路由规则（核心）
```

## 二、逐文件精读

### 1. pom.xml —— 三个依赖各司其职

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>          # ① 网关本体（基于 WebFlux/Netty）
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>  # ② 从 Nacos 发现服务
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>     # ③ lb:// 协议的执行者（负载均衡选实例）
</dependency>
```

**关键认知**：
- ②③ 组合才有 `lb://`：② 负责"查到 service-gateway-provider 有哪些实例"，③ 负责"从列表里按策略挑一个"
- 没有 ③ → 转发报 **503 Service Unavailable**（SCA 2021.x 移除了 Ribbon 后的经典坑）
- 没有 ② → `lb://` 查不到实例，同样是 503
- **没有 spring-boot-starter-web**：Gateway 是响应式栈（Netty），和 Tomcat 互斥，引了直接启动失败

### 2. application.yml —— 路由三要素（本模块灵魂）

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: nacos-route                        # 路由唯一标识（日志/管理用）
          uri: lb://service-gateway-provider    # 目标：按服务名去 Nacos 找实例（lb = load balancer）
          predicates:                            # 断言：请求满足什么条件才走这条路由
            - Path=/nacos/**                    #   路径以 /nacos/ 开头
          filters:                               # 过滤器：转发前后对请求/响应做加工
            - StripPrefix=1                      #   转发前剥掉第一级路径 /nacos
```

**一次请求 `GET /nacos/echo/hi` 的完整流转（务必背下来）**：

```
收到请求 → Predicate 匹配 /nacos/** ✓（命中 nacos-route）
        → Filter StripPrefix=1：/nacos/echo/hi 变成 /echo/hi
        → uri 是 lb://：向 Nacos 查 service-gateway-provider 的实例列表
                        假设得到 [192.168.1.5:18086, 192.168.1.6:18087]
        → LoadBalancer 按策略（默认轮询）选出 192.168.1.5:18086
        → 实际转发 GET http://192.168.1.5:18086/echo/hi
        → Provider 返回 "hello Nacos Discovery hi" → 原路返回给调用方
```

**Predicate / Filter 的常用家族**（面试高频，建议都敲一遍）：

| 类型 | 示例 | 含义 |
|------|------|------|
| Path | `Path=/nacos/**` | 路径匹配（最常用） |
| Method | `Method=GET,POST` | HTTP 方法匹配 |
| Header | `Header=X-Token, \d+` | 请求头存在/匹配正则 |
| Query | `Query=name, zhang.` | 查询参数匹配 |
| Time | `After=2025-01-01T...` | 定时开关路由（灰度常用） |

| Filter | 示例 | 含义 |
|--------|------|------|
| StripPrefix | `StripPrefix=1` | 剥 N 级前缀（对外前缀 ≠ 对内路径时用） |
| AddRequestHeader | `AddRequestHeader=X-Gateway, nacos` | 加请求头（如透传用户信息） |
| PrefixPath | `PrefixPath=/api` | 反向操作：加前缀 |
| RewritePath | `RewritePath=/nacos/(?<seg>.*), /$\{seg}` | 正则重写路径（更灵活的 StripPrefix） |
| SetStatus | `SetStatus=401` | 改响应状态码 |

### 3. GatewayApplication.java —— @LoadBalancerClients

```java
@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClients({
        @LoadBalancerClient("service-gateway-provider")   # 显式声明要对哪个服务做负载均衡
})
```
- 作用：为名为 `service-gateway-provider` 的服务定制负载均衡客户端
- 在这个简单例子里其实可省略（默认行为即可），但它是**自定义负载均衡策略的扩展点**：
  ```java
  @LoadBalancerClient(value = "service-gateway-provider",
      configuration = MyLbConfig.class)   // 里面可定义自定义 ServiceInstanceListSupplier/策略
  ```

### 4. management 配置 —— 网关可观测性

```yaml
management:
  endpoints:
    web:
      exposure:
        include: '*'      # 暴露所有 actuator 端点
```
最实用的两个端点：
- `GET /actuator/gateway/routes` —— 查看生效的完整路由表（排查"为什么没走到这条路由"）
- `GET /actuator/gateway/globalfilters`、`/routefilters` —— 查看已注册的过滤器

## 三、动手验证（配合 Provider 一起）

```bash
# 1. 看路由表
curl http://localhost:18085/actuator/gateway/routes

# 2. 走网关访问（观察 StripPrefix）
curl http://localhost:18085/nacos/echo/hi          # → hello Nacos Discovery hi

# 3. 故意走不存在的路径（观察 Predicate 不命中 → 404）
curl http://localhost:18085/xxx/echo/hi            # → 404

# 4. 多实例负载均衡：再起一个 Provider（-Dserver.port=18087）
for i in $(seq 1 6); do curl -s http://localhost:18085/nacos/echo/hi; echo; done
# 两个 Provider 控制台交替收到请求（默认轮询）

# 5. 停掉一个 Provider 实例，继续请求 → 流量全部到另一个（动态故障转移）

# 6. 触发异常看网关行为
curl "http://localhost:18085/nacos/divide?a=1&b=0"  # → 500（可思考如何统一处理）
```

## 四、学完本模块你应收获

1. 会读/写一条路由：**id + uri + predicates + filters** 四件套
2. 彻底理解 `lb://服务名` 的含义：服务名寻址 + 客户端负载均衡
3. 能独立解释"为什么加一个实例网关不用改配置"（因为实例列表是启动后实时从 Nacos 拉的）
4. 会用 `/actuator/gateway/routes` 排查路由问题
5. 理解 StripPrefix 的价值：**对外暴露统一前缀，对内保持服务自身路径干净**

## 五、日常怎么用 / 企业怎么用

**日常开发**：
- 前端只配网关一个 baseURL：`http://gateway:18085`，按服务加前缀 `/nacos/**`、`/order/**`
- 本地调试路由改动 → 改 yml 重启；或用 `nacos-config` 把路由放进配置中心实现**动态刷新**
- 排查 404/503 的三板斧：`/actuator/gateway/routes` 看路由 → Nacos 控制台看实例健康 → 直连目标服务验证路径

**企业典型用法（网关上统一做的事）**：

| 能力 | 实现方式 |
|------|----------|
| 统一鉴权 | 自定义 GlobalFilter 校验 JWT，无效直接 401 |
| 接口限流 | RequestRateLimiter（Redis）或 Sentinel 网关流控 |
| 灰度发布 | 按 Header/Cookie/权重把流量路由到指定版本实例 |
| 访问日志/审计 | GlobalFilter 记录 traceId、耗时、状态码 |
| 跨域 CORS | 统一在网关配置，业务服务不再各自配 |
| 黑白名单 | Filter 里校验 IP/UA |
| 隐藏内部结构 | StripPrefix/RewritePath 让对外路径与内部实现解耦 |

**部署形态**：网关无状态 → 多实例 + Nginx/SLB 前置；网关本身也注册到 Nacos 便于治理。

## 六、注意什么 / 什么时候用

1. **不能引 spring-boot-starter-web**（WebFlux 互斥）；网关代码里也不要写阻塞 IO（JDBC 等），会拖垮 Netty 事件循环
2. **lb:// 必须配 loadbalancer 依赖**，否则 503
3. **路由顺序**：多条路由都匹配时，小的 order 优先；断言写得含糊容易"被别的路由抢走"
4. **StripPrefix 位数**：对外 `/nacos/echo/hi` 对内 `/echo/hi`，数错直接 404
5. **服务名大小写敏感**：`lb://Service-Gateway-Provider` ≠ `service-gateway-provider`
6. **实例刚启动/刚下线有秒级窗口**：Nacos 推送有延迟，可能短暂打到已下线实例 → 生产要配合重试或熔断
7. **网关要轻**：业务逻辑不要写在网关里，只做"横切面"的事；重逻辑堆在网关会形成单点瓶颈
8. **要不要网关**：服务少（1~2 个）用 Nginx 就够；服务多了、需要统一治理入口时才上 Gateway

## 七、进阶练习（下一步动手方向）

- [ ] 写一个 `GlobalFilter` 打印每个请求的耗时和目标实例
- [ ] 写一个鉴权 Filter：无 `Authorization` 头返回 401
- [ ] 把 routes 配置挪到 Nacos 配置中心，实现改路由不重启网关
- [ ] 集成 Sentinel 做网关限流：QPS 超阈值返回 429
- [ ] 用 Weight 断言做一次"灰度发布"实验
