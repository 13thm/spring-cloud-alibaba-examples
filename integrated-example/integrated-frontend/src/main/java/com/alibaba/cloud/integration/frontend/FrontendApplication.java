/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.integration.frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 前端页面服务启动类
 *
 * <p>纯 Thymeleaf 页面服务：不含任何业务逻辑，
 * 只负责渲染三个演示页面（下单 / RocketMQ 点赞 / Sentinel 限流），
 * 页面里的按钮再通过网关(30010)调用各微服务接口。</p>
 *
 * <p>把它独立成服务（而不是塞进某个业务服务）是常见的前后端分离习惯：
 * 页面与业务服务各自独立部署、独立扩缩容。</p>
 *
 * @author HuangSir
 * @date 2022-09-08 14:11
 */
@SpringBootApplication
public class FrontendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrontendApplication.class, args);
	}

}
