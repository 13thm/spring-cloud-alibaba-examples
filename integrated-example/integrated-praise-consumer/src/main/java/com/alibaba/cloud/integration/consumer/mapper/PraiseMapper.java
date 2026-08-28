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

package com.alibaba.cloud.integration.consumer.mapper;

import java.sql.Timestamp;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import org.springframework.stereotype.Repository;

/**
 * 点赞业务表 item 的 MyBatis Mapper（integrated_praise 库）。
 *
 * <p>praise 字段做"点赞数 +1"的自增更新 —— 消费一条消息就 +1，
 * 消费速率由 RocketMQ binder 配置控制，从而保护数据库。</p>
 *
 * @author TrevorLink
 */
@Mapper
@Repository
public interface PraiseMapper {

	/**
	 * 点赞数 +1（消费消息的核心写操作）。
	 * @param itemId 商品 ID
	 * @param updateTime 更新时间
	 * @return 更新行数
	 */
	@Update("update item set praise = praise+1,update_time=#{updateTime} where id = #{itemId}")
	int praiseItem(@Param("itemId") Integer itemId,
			@Param("updateTime") Timestamp updateTime);

	/**
	 * 查询指定商品的点赞数。
	 * @param itemId 商品 ID
	 * @return 点赞总数
	 */
	@Select("select praise from item where id = #{itemId}")
	int getPraise(@Param("itemId") Integer itemId);

}
