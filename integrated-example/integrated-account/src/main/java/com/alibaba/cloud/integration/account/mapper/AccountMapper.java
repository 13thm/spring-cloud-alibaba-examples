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

package com.alibaba.cloud.integration.account.mapper;

import java.sql.Timestamp;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import org.springframework.stereotype.Repository;

/**
 * 账户表 account 的 MyBatis Mapper。
 *
 * <p>亮点：扣减余额的 UPDATE 里带了 {@code AND money >= ${price}} 条件 ——
 * 数据库层面兜底防止扣成负数（即使并发下校验失效，SQL 条件也拦得住）。
 * 注意此处 ${price} 是字符串拼接（演示用途），生产环境应统一用 #{} 预编译防注入。</p>
 *
 * @author TrevorLink
 */
@Mapper
@Repository
public interface AccountMapper {

	/**
	 * 查询指定用户的余额。
	 * @param userId 用户 ID
	 * @return 余额；用户不存在时返回 null
	 */
	@Select("SELECT money FROM account WHERE user_id = #{userId}")
	Integer getBalance(@Param("userId") String userId);

	/**
	 * 扣减余额（带余额充足条件）。
	 *
	 * @param userId 用户 ID
	 * @param price 扣减金额
	 * @param updateTime 更新时间
	 * @return 实际更新行数（余额不足时条件不满足，返回 0）
	 */
	@Update("UPDATE account SET money = money - #{price},update_time = #{updateTime} WHERE user_id = #{userId} AND money >= ${price}")
	int reduceBalance(@Param("userId") String userId, @Param("price") Integer price,
			@Param("updateTime") Timestamp updateTime);

}
