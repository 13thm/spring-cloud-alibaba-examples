package com.thm.cloud.integration.storage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

/**
 * 库存表 storage 的 MyBatis Mapper（注解 SQL 方式，无 XML）。
 *
 * <p>要点：UPDATE 会被 Seata 的数据源代理拦截 —— 执行前自动生成
 * 前镜像/后镜像写入 undo_log 表，用于全局回滚时的反向补偿。</p>
 *
 * @author thm
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
	 * <p>SQL 没有显式 {@code count >= #{count}} 条件，库存不足的防线在
	 * Service 层的 checkStock 前置校验；若实际更新行数为 0，
	 * Service 层视为扣减失败抛业务异常。</p>
	 *
	 * @param commodityCode 商品编码
	 * @param count 扣减数量
	 * @param updateTime 更新时间
	 * @return 实际更新的行数（0 表示扣减未生效）
	 */
	@Update("UPDATE storage SET count = count - #{count}, update_time = #{updateTime} WHERE commodity_code = #{commodityCode}")
	int reduceStock(@Param("commodityCode") String commodityCode,
			@Param("count") Integer count, @Param("updateTime") Timestamp updateTime);

}
