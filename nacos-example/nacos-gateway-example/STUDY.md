# Nacos + Spring Cloud Gateway 学习路线总览

> 本模块演示：**Spring Cloud Gateway 如何基于 Nacos 注册中心实现动态服务路由（lb:// 协议 + 客户端负载均衡）**

---

## 一、先搞懂三个角色（宏观认知）

```
浏览器/Curl
    │
    ▼ http://localhost:18085/nacos/echo/hi
┌─────────────────────────────┐
│  nacos-gateway-discovery    │  网关：service-gateway (18085)
│  收到 /nacos/** 的请求       │  ① 断言匹配 → ② 剥掉一级前缀 → ③ 把
│                             │     lb://service-gateway-provider
│                             │     解析成真实 IP:Port 并转发
└──────────┬──────────────────┘
           │ ②③ 向 Nacos 查询 service-gateway-provider 的实例列表
           ▼
     ┌───────────┐
     │   Nacos   │  注册中心：127.0.0.1:8848
     │  服务列表  │  维护所有服务的"电话簿"
     └─────▲─────┘
           │ ① 启动时注册自己 (IP + 18086)
┌──────────┴──────────────────┐
│  nacos-gateway-provider     │  业务服务：service-gateway-provider (18086)
│  EchoController             │  真正干活的业务服务
└─────────────────────────────┘
```

一句话总结：**网关是"总机"，Nacos 是"通讯录"，Provider 是"接线员"**。
网关自己不知道 Provider 的 IP，它只认服务名，IP 列表实时从 Nacos 拿。

---

## 二、学习路线（建议按顺序，2~3 天）

| 阶段 | 内容 | 位置 | 目标 |
|------|------|------|------|
| 第 1 步 | 学 Provider：服务如何注册到 Nacos | `nacos-gateway-provider-example/STUDY.md` | 理解服务注册、@EnableDiscoveryClient、直接调用 |
| 第 2 步 | 学 Gateway：路由三要素 | `nacos-gateway-discovery-example/STUDY.md` | 理解 Route/Predicate/Filter、lb://、StripPrefix |
| 第 3 步 | 联调验证 | 见下文"动手实验" | 断点观察请求流转、Nacos 控制台看服务列表 |
| 第 4 步 | 进阶思考 | 各 STUDY.md 末尾"进阶" | 知道生产上还要加什么 |

---

## 三、动手实验（必做）

### 准备
1. 本地启动 Nacos Server（`startup.cmd -m standalone`），控制台 http://localhost:8848/nacos
2. 先启动 `ProviderApplication`，再启动 `GatewayApplication`

### 实验 1：绕过网关直连 Provider（理解"没有网关时"）
```bash
curl http://localhost:18086/echo/hi
# → hello Nacos Discovery hi
```

### 实验 2：通过网关访问（理解路由 + StripPrefix）
```bash
curl http://localhost:18085/nacos/echo/hi
# → hello Nacos Discovery hi
```
思考：为什么是 `/nacos/echo/hi` 而不是 `/echo/hi`？
因为路由 Predicate 匹配 `Path=/nacos/**`，且 `StripPrefix=1` 把 `/nacos` 剥掉了。

### 实验 3：动态扩容（理解 lb:// 的意义）
```bash
# 用不同端口再起一个 Provider 实例（IDEA 里 Copy Configuration，加 VM 参数）
-Dserver.port=18087
```
多次请求 `http://localhost:18085/nacos/echo/hi`，到两个实例打断点，
或看控制台日志 —— **网关代码零改动，请求自动轮询打到两个实例**，这就是 `lb://` + 负载均衡。

### 实验 4：下线一个实例（理解动态发现）
把 18086 的实例停掉，Nacos 心跳超时后（默认 5 秒不健康/15 秒剔除），继续请求网关 —— 流量全部落到 18087，**无感知故障转移**。

### 实验 5：看网关的路由表
```bash
curl http://localhost:18085/actuator/gateway/routes
```
能看到 `nacos-route` 这条路由的完整定义（id、uri、predicates、filters）。

---

## 四、学完你应收获什么（Checklist）

- [ ] 能画出最上面的架构图，说清一次请求的完整流转
- [ ] 能说清 `lb://service-gateway-provider` 和 `http://192.168.x.x:18086` 的区别（服务名 vs 硬编码地址）
- [ ] 理解 Route（路由）、Predicate（断言）、Filter（过滤器）三要素
- [ ] 理解 StripPrefix 的作用和"为什么网关要统一路径前缀"
- [ ] 能独立完成"加一个新服务 + 在网关配一条路由"的操作
- [ ] 明白为什么企业里前端只需要知道网关地址

---

## 五、日常怎么用 / 企业怎么用

### 日常开发
- 前后端联调时，前端只需配一个网关地址 + 服务前缀，例如 `/api/order/**`、`/api/user/**`
- 本地想单独调某服务，仍然可以直连该服务端口绕过网关
- 加新服务 = 写服务 + 网关加一条路由（或启用服务名自动路由）

### 企业典型架构
```
Nginx/SLB → Gateway 集群(无状态,可横向扩) → Nacos ←→ 各微服务集群
                │
                ├── 统一鉴权 (JWT 校验 Filter)
                ├── 限流熔断 (RequestRateLimiter / Sentinel)
                ├── 灰度发布 (按 Header/权重分流)
                ├── 日志审计 (GlobalFilter 记录访问日志)
                └── 跨域配置 (统一在网关处理)
```
网关是所有流量的**唯一入口（North-South Traffic）**，业务服务之间互调（East-West）一般走 OpenFeign。

---

## 六、什么时候用 / 不用

**用网关（+Nacos）的信号：**
- 服务数 ≥ 3，前端/客户端不想记一堆 IP 端口
- 需要统一入口做鉴权、限流、日志、灰度
- 服务会动态扩缩容（K8s、弹性伸缩），IP 不固定

**不需要的信号：**
- 单体应用 / 只有一两个服务：直接 Nginx 反代即可，引入网关是过度设计
- 内部服务间同步调用：网关是给"外部入口"用的，不是给服务间调用用的

---

## 七、注意事项（易踩的坑）

1. **Web 依赖冲突**：Gateway 基于 WebFlux（Netty，响应式），不能引 `spring-boot-starter-web`（Tomcat/Servlet），两者互斥，启动直接报错。Provider 才用 web。
2. **lb:// 必须有负载均衡器**：SCA 2021+ 移除了 Ribbon，必须引入 `spring-cloud-starter-loadbalancer`，否则转发报 503。
3. **StripPrefix 数错**：`StripPrefix=1` 剥一级。前缀剥多/剥少都会 404，排查时先 `curl` 直连 Provider 确认真实路径。
4. **服务名要一致**：路由里 `lb://service-gateway-provider` 必须和 Provider 的 `spring.application.name` 完全一致（区分大小写），否则 `Service Unavailable`。可在 Nacos 控制台核对服务名和实例健康状态。
5. **Nacos 地址密码**：示例写的 nacos/nacos，生产环境一定改掉并走配置中心加密。

---

## 八、下一步学习什么

- `nacos-config`：配置中心（配合网关做路由的动态刷新）
- Gateway 的自定义 `GlobalFilter`（鉴权）与 `RequestRateLimiter`（限流）
- OpenFeign + LoadBalancer：服务间声明式调用
- Sentinel：网关流量防护、熔断降级
