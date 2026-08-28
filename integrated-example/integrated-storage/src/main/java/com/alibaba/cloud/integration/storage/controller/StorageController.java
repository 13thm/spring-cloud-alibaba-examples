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

package com.alibaba.cloud.integration.storage.controller;

import com.alibaba.cloud.integration.common.BusinessException;
import com.alibaba.cloud.integration.common.Result;
import com.alibaba.cloud.integration.storage.dto.StorageDTO;
import com.alibaba.cloud.integration.storage.service.StorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存服务 HTTP 接口。
 *
 * <p>两个入口：</p>
 * <ul>
 *   <li>POST /storage/reduce-stock —— 扣减库存，被订单服务 Feign 调用（分布式事务链路）；</li>
 *   <li>GET  /storage/?commodityCode=x —— 查询剩余库存，被前端经网关查询展示。</li>
 * </ul>
 *
 * @author TrevorLink
 */
@RestController
@RequestMapping("/storage")
public class StorageController {

	@Autowired
	private StorageService storageService;

	/**
	 * 扣减库存接口。
	 *
	 * <p>异常处理策略：Service 抛出的业务异常（如库存不足）在这里"拦截"，
	 * 转成 code=2003 的失败 Result 返回 —— 注意此时方法正常返回不再向上抛，
	 * 调用方（订单服务）通过判断 Result.code 得知失败后自己抛异常触发全局回滚。</p>
	 */
	@PostMapping("/reduce-stock")
	public Result<?> reduceStock(@RequestBody StorageDTO storageDTO) {
		try {
			storageService.reduceStock(storageDTO.getCommodityCode(),
					storageDTO.getCount());
		}
		catch (BusinessException e) {
			// 库存不足等业务失败 → 统一失败响应（订单服务看到 2003 后抛异常，Seata 回滚全局事务）
			return Result.failed(e.getMessage());
		}
		return Result.success("");
	}

	/**
	 * 查询指定商品的剩余库存。
	 * @param commodityCode 商品编码
	 */
	@GetMapping("/")
	public Result<?> getRemainCount(String commodityCode) {
		return storageService.getRemainCount(commodityCode);
	}

}
