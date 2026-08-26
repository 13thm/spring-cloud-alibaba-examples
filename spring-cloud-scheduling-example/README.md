# Spring Cloud Alibaba Scheduling Example

## 项目说明

Spring Cloud Alibaba Scheduling 提供了基于 Spring Scheduling 的定时任务调度能力，支持分布式场景下的定时任务调度，为分布式场景下的定时任务调度服务提供快速接入方案。

目前提供基于开源shedlock分布式抢锁模式，以及阿里云上SchedulerX服务的 [快速接入](https://sca.aliyun.com/docs/2023/user-guide/schedulerx/quick-start/) ，后续将提供更多实现的开源方案接入。

## 应用依赖

### 接入 `spring-cloud-starter-alibaba-schedulerx`

在项目 pom.xml 中加入以下依赖：

   ```xml
   <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-schedulerx</artifactId>
   </dependency>
   ```

## 配置说明

在Example工程中提供shedlock和schedulerx的两种接入配置模式（***二选一***），在`application.yaml`中选择需要的接入模式配置文件，案例中默认采用开源的shedlock方案。

### 方案一、分布式shedlock接入配置说明

在 application-schedulerx.yml 配置文件中修改以下配置：
   ```yaml
   spring:
      cloud:
         scheduling:
            # Distributed mode: shedlock, schedulerx
            # Set config value: shedlock
            distributed-mode: shedlock
      datasource:
         driver-class: com.mysql.cj.jdbc.Driver
         url: {jdbc_url}
         username: {jdbc.username}
         password: {jdbc.password}
   ```
使用时请需要替换`{jdbc_url}`、`{jdbc.username}`、`{jdbc.password}`为实际自有的数据库连接信息。

>️ 注意：如未创建数据库，请先手动创建数据实例。

### 方案二、云产品SchedulerX接入配置说明
在 application-schedulerx.yml 配置文件中修改以下配置：
   ```yaml
   spring:
      cloud:
         scheduling:
            # Distributed mode: shedlock, schedulerx
            # Set config value: schedulerx
            distributed-mode: schedulerx
            schedulerx:
               # This configuration is required, Please get it from aliyun schedulerx console
               endpoint: acm.aliyun.com
               namespace: aad167f6-xxxx-xxxx-xxxx-xxxxxxxxx
               groupId: xxxxx
               appKey: PZm1XXXXXXXXXXXX
               # Optional config, if you need to sync task to schedulerx
               # task-sync: true
               # region-id: public
               # aliyun-access-key: XXXXXXXXXXXX
               # aliyun-secret-key: XXXXXXXXXXXX
               # task-model-default: standalone
   ```
阿里云上产品每个用户开通后都会有免费额度，详细云上产品接入配置使用说明，请参考：[阿里云SchedulerX Spring定时任务](https://help.aliyun.com/zh/schedulerx/user-guide/spring-jobs)

## 启动应用

在完成上述接入选择和配置后，直接运行Example中的 `ScheduleApplication`类即可启动运行。本案例工程中的`SimpleJob`类包含了两个每分钟执行一次的Spring定时任务，启动后可得到如下日志：
```text
2024-05-17T11:20:59.981+08:00  INFO 66613 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-4] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-17 11:20:59 do job1...
2024-05-17T11:20:59.985+08:00  INFO 66613 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-1] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-17 11:20:59 do job2...
```
### 分布式运行验证

在IDEA环境中创建两个应用启动项，各自分别配置启动参数`--server.port={应用端口}`，启动相应的应用进程。案例工程默认采用`shedlock`，
我们可以直接看到`job1`两个应用都会同时触发，而`job2`添加了`@SchedulerLock`注解则会同一时间点只会在一个应用进程中执行。
![idea-server-port](images/idea-server-port.png)

- ScheduleApplication-1，启动参数：`--server.port=18080`，定时任务运行日志如下：
```text
2024-05-20T14:02:00.003+08:00  INFO 80520 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-4] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:02:00 do job1...
2024-05-20T14:03:00.008+08:00  INFO 80520 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-4] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:03:00 do job1...
2024-05-20T14:03:00.008+08:00  INFO 80520 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-1] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:03:00 do job2...
2024-05-20T14:04:00.006+08:00  INFO 80520 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-3] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:04:00 do job1...
2024-05-20T14:04:00.010+08:00  INFO 80520 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-2] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:04:00 do job2...
2024-05-20T14:05:00.003+08:00  INFO 80520 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-5] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:05:00 do job1...
```
- ScheduleApplication-2，启动参数：`--server.port=18081`，定时任务运行日志如下：
```text
2024-05-20T14:02:00.003+08:00  INFO 80596 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-4] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:02:00 do job1...
2024-05-20T14:02:00.008+08:00  INFO 80596 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-3] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:02:00 do job2...
2024-05-20T14:03:00.004+08:00  INFO 80596 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-5] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:03:00 do job1...
2024-05-20T14:04:00.006+08:00  INFO 80596 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-3] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:04:00 do job1...
2024-05-20T14:05:00.004+08:00  INFO 80596 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-2] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:05:00 do job1...
2024-05-20T14:05:00.007+08:00  INFO 80596 --- [spring-cloud-alibaba-schedule-example] [ sca-schedule-4] c.a.c.examples.schedule.job.SimpleJob    : time=2024-05-20 14:05:00 do job2...
```
