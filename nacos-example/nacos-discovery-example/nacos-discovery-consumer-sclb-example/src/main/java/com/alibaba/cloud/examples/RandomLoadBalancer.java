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

package com.alibaba.cloud.examples;

import java.util.List;
import java.util.Random;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

/**
 * Self-defined randomLoadBalancer.
 *
 * @author fangjian0423, MieAh
 */
public class RandomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

	private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

	private final String serviceId;

	private final Random random;

	public RandomLoadBalancer(
			ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
			String serviceId) {
		this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
		this.serviceId = serviceId;
		this.random = new Random();
	}

	@Override
	public Mono<Response<ServiceInstance>> choose(
			org.springframework.cloud.client.loadbalancer.Request request) {
		ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
				.getIfAvailable(NoopServiceInstanceListSupplier::new);

		return supplier.get().next().map(this::getInstanceResponse);
	}
	/*
	 Mono 这个响应式编程的意思是：数据想流水一样，自动推送给我，这个 Mono 代表一个异步序列里面最多只包含 0 或 1 个元素。
	 （如果包含多个元素，则使用 Flux）supplier.get().next()：这行代码并没有立刻去数据库或注册中心查数据，
	 它只是构建了一个“获取数据的管道/计划”，并返回一个 Mono 对象
	 .map(this::getInstanceResponse)：这也是一个操作符，意思是“等数据真的到了，再帮我把它转换成 Response 格式”
	 */

	@Override
	public Mono<Response<ServiceInstance>> choose() {
		ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
				.getIfAvailable(NoopServiceInstanceListSupplier::new);
		return supplier.get().next().map(this::getInstanceResponse);
	}

	private Response<ServiceInstance> getInstanceResponse(
			List<ServiceInstance> instances) {
		if (instances.isEmpty()) {
			return new EmptyResponse();
		}
		ServiceInstance instance = instances.get(random.nextInt(instances.size()));

		return new DefaultResponse(instance);
	}

}
