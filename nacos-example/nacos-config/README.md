# nacos-example

Spring Cloud Alibaba **Nacos 配置中心（Nacos Config）** 示例工程。

本工程用于演示如何在 Spring Boot / Spring Cloud Alibaba 应用中接入 Nacos 作为配置中心，实现配置的集中管理和**动态刷新**（修改 Nacos 控制台中的配置后，应用无需重启即可感知到变化）。

## 模块说明

| 模块 | 说明 |
|------|------|
| `nacos-config` | Nacos 配置中心示例，演示 4 种常见的配置使用方式 |

## 示例内容

| 类 | 演示内容 | 访问方式 |
|----|---------|---------|
| `ValueAnnotationExample` | 使用 `@Value` + `@RefreshScope` 注解获取配置，配置变更后自动刷新 | `GET /nacos/annotation` |
| `BeanAutoRefreshConfigExample` | 使用 `@ConfigurationProperties` 绑定配置 Bean，自动刷新（无需 `@RefreshScope`） | `GET /nacos/bean` |
| `ConfigListenerExample` | 通过 `NacosConfigManager` 注册 `Listener`，监听指定 dataId 的配置变更并打印日志 | 启动后自动生效，观察控制台日志 |
| `DockingInterfaceExample` | 直接对接 Nacos 原生 API（`ConfigService`），支持查询、发布、删除配置及动态添加监听器 | 见下方接口列表 |

- ValueAnnotationExample  这个解释了这个的用法怎么配置
- BeanAutoRefreshConfigExample  可以使用bean的方式进行获取
- ConfigListenerExample 讲解了这个动态是如何生效的是通过监听的方法，可以在配置之前重写这个方法，制定一些特殊功能。
- DockingInterfaceExample ：案例一共演示 4 种能力

1. **getConfig**：手动从 Nacos 服务端拉取指定 dataId+group 的配置内容
2. **publishConfig**：代码里往 Nacos 服务端发布 / 新增 / 修改配置（等价控制台发布）
3. **removeConfig**：代码里删除 Nacos 上的配置
4. **addListener**：运行时动态给某个配置注册变更监听器，配置修改就收到回调

## 案例的定位

1. 属于**技术演示 Demo，不是业务模板**，用来学习 Nacos Config 底层 Java API 如何调用。

### 原生 API 接口（`/nacos`）

- `GET /nacos/getConfig?dataId=xxx&group=xxx` —— 查询配置
- `GET /nacos/publishConfig?dataId=xxx&group=xxx&content=xxx` —— 发布配置
- `GET /nacos/removeConfig?dataId=xxx&group=xxx` —— 删除配置
- `GET /nacos/listener?dataId=xxx&group=xxx` —— 为指定 dataId 添加配置变更监听

## 技术栈

- JDK 17
- Spring Boot 3.2.9
- Spring Cloud Alibaba 2023.0.1.0（`spring-cloud-starter-alibaba-nacos-config`）

## 快速开始

### 1. 启动 Nacos Server

本地启动 Nacos（默认地址 `127.0.0.1:8848`，账号密码 `nacos/nacos`）。

### 2. 创建配置

在 Nacos 控制台创建配置：

- **Data ID**：`nacos-config-example.properties`
- **Group**：`DEFAULT_GROUP`
- **配置内容**（示例）：

```properties
spring.cloud.nacos.config.serverAddr=127.0.0.1:8848
spring.cloud.nacos.config.prefix=nacos-config-example
spring.cloud.nacos.config.group=DEFAULT_GROUP
spring.cloud.nacos.config.namespace=
```

> 上述 key 与 `NacosConfigInfo` 类的属性一一对应，可通过 `/nacos/bean`、`/nacos/annotation` 接口查看效果。

### 3. 启动应用

```bash
cd nacos-config
mvn spring-boot:run
```

应用启动端口：`18084`（在 `nacos-config/src/main/resources/application.yaml` 中配置）。

关键配置说明：

```yaml
spring:
  config:
    import:
      # 引入 Nacos 中的配置，并开启自动刷新
      - nacos:nacos-config-example.properties?refreshEnabled=true
```

### 4. 验证动态刷新

1. 访问 `http://localhost:18084/nacos/bean`，查看当前配置值；
2. 在 Nacos 控制台修改 `nacos-config-example.properties` 中的某个值并发布；
3. 再次访问该接口，观察返回值已自动更新（无需重启应用）；同时控制台会打印 `ConfigListenerExample` 监听到的配置变更日志。

## 目录结构

```
nacos-example
└── nacos-config
    └── src/main
        ├── java/com/thm/cloud
        │   ├── NacosConfigApplication.java   # 启动类
        │   ├── example
        │   │   ├── ValueAnnotationExample.java        # @Value + @RefreshScope 示例
        │   │   ├── BeanAutoRefreshConfigExample.java  # @ConfigurationProperties 自动刷新示例
        │   │   ├── ConfigListenerExample.java         # 配置变更监听示例
        │   │   └── DockingInterfaceExample.java       # Nacos 原生 API 对接示例
        │   └── model
        │       └── NacosConfigInfo.java               # 配置属性映射类
        └── resources
            └── application.yaml                      # 应用配置
```
