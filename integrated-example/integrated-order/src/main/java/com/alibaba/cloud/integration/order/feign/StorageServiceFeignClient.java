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

package com.alibaba.cloud.integration.order.feign;

import com.alibaba.cloud.integration.common.Result;
import com.alibaba.cloud.integration.order.feign.dto.StorageDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 库存服务的 Feign 客户端（订单 → 库存）。
 *
 * <p>用法同 {@link AccountServiceFeignClient}：按服务名从 Nacos 发现实例，
 * 声明式调用 /storage/reduce-stock 扣减库存，Seata 自动透传 XID。</p>
 *
 * @author TrevorLink
 */
@FeignClient(name = "integrated-storage")
public interface StorageServiceFeignClient {

	/**
	 * 调用库存服务扣减库存。
	 * 对应 StorageController#reduceStock。
	 * @param productReduceStockDTO 扣减请求（commodityCode + count）
	 * @return 统一响应，code=2003 表示库存不足等业务失败
	 */
	@PostMapping("/storage/reduce-stock")
	Result<?> reduceStock(@RequestBody StorageDTO productReduceStockDTO);

}
