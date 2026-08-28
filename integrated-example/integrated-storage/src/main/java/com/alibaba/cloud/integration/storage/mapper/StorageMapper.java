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

package com.alibaba.cloud.integration.storage.mapper;

import java.sql.Timestamp;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import org.springframework.stereotype.Repository;

/**
 * 库存表 storage 的 MyBatis Mapper（注解 SQL 方式，无 XML）。
 *
 * <p>要点：两条 SQL 都会被 Seata 的数据源代理拦截 —— 执行 UPDATE 前自动生成
 * 前镜像/后镜像写入 undo_log，用于全局回滚时的反向补偿。</p>
 *
 * @author TrevorLink
 */
@Mapper
@Repository
public interface StorageMapper {

	/**
	 * 查询指定商品的剩余库存数。
	 * @param commodityCode 商品编码
	 * @return 库存数量；商品不存在时返回 null
	 */
	@Select("SELECT `count` FROM storage WHERE commodity_code = #{commodityCode}")
	Integer getStock(@Param("commodityCode") String commodityCode);

	/**
	 * 扣减库存。
	 *
	 * <p>SQL 中没有显式的 {@code count >= #{count}} 条件，
	 * 并发不足的防线在 Service 层的 checkStock 前置校验 + 乐观思路；
	 * 若实际更新行数为 0，Service 层视为扣减失败抛业务异常。</p>
	 *
	 * @param commodityCode 商品编码
	 * @param count 扣减数量
	 * @param updateTime 更新时间
	 * @return 实际更新的行数（0 表示扣减未生效）
	 */
	@Update("UPDATE storage SET count = count - #{count},update_time=#{updateTime} WHERE commodity_code = #{commodityCode}")
	int reduceStock(@Param("commodityCode") String commodityCode,
			@Param("count") Integer count, @Param("updateTime") Timestamp updateTime);

}
