package com.thm.cloud.integration.consumer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

/**
 * 点赞业务表 item 的 MyBatis Mapper（integrated2_praise 库）。
 *
 * <p>praise 字段做"点赞数 +1"的自增更新 —— 消费一条消息就 +1，
 * 消费速率由 RocketMQ binder 配置控制，从而保护数据库。</p>
 *
 * @author thm
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
	@Update("UPDATE item SET praise = praise + 1, update_time = #{updateTime} WHERE id = #{itemId}")
	int praiseItem(@Param("itemId") Integer itemId, @Param("updateTime") Timestamp updateTime);

	/**
	 * 查询指定商品的点赞数。
	 * @param itemId 商品 ID
	 * @return 点赞总数
	 */
	@Select("SELECT praise FROM item WHERE id = #{itemId}")
	int getPraise(@Param("itemId") Integer itemId);

}
