package com.thm.cloud.integration.account.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

/**
 * 账户表 account 的 MyBatis Mapper。
 *
 * <p>亮点：扣减余额的 UPDATE 里带了 {@code AND money >= #{price}} 条件 ——
 * 数据库层面兜底防止扣成负数（即使并发下校验失效，SQL 条件也拦得住）。
 * 这里统一用 #{} 预编译占位符防 SQL 注入。</p>
 *
 * @author thm
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
	@Update("UPDATE account SET money = money - #{price}, update_time = #{updateTime} WHERE user_id = #{userId} AND money >= #{price}")
	int reduceBalance(@Param("userId") String userId, @Param("price") Integer price,
			@Param("updateTime") Timestamp updateTime);

}
