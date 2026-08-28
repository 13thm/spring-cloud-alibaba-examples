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

package com.alibaba.cloud.integration.account.controller;

import com.alibaba.cloud.integration.account.dto.AccountDTO;
import com.alibaba.cloud.integration.account.service.AccountService;
import com.alibaba.cloud.integration.common.BusinessException;
import com.alibaba.cloud.integration.common.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账户服务 HTTP 接口。
 *
 * <ul>
 *   <li>POST /account/reduce-balance —— 扣减余额，被订单服务 Feign 调用；</li>
 *   <li>GET  /account/?userId=x —— 查询剩余余额，前端展示用。</li>
 * </ul>
 *
 * @author TrevorLink
 */
@RestController
@RequestMapping("/account")
public class AccountController {

	@Autowired
	private AccountService accountService;

	/**
	 * 扣减账户余额。
	 *
	 * <p>与库存服务同款模式：业务异常在 Controller 层转为 code=2003 的失败响应，
	 * 由调用方（订单服务）判断 code 后抛异常，触发 Seata 全局回滚。</p>
	 */
	@PostMapping("/reduce-balance")
	public Result<?> reduceBalance(@RequestBody AccountDTO accountDTO) {
		try {
			accountService.reduceBalance(accountDTO.getUserId(), accountDTO.getPrice());
		}
		catch (BusinessException e) {
			// 余额不足等失败 → 统一失败响应
			return Result.failed(e.getMessage());
		}
		return Result.success("");
	}

	/**
	 * 查询指定用户的剩余余额。
	 * @param userId 用户 ID
	 */
	@GetMapping("/")
	public Result<?> getRemainAccount(String userId) {
		return accountService.getRemainAccount(userId);
	}

}
