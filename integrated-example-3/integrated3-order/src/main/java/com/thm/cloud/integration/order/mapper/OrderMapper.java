package com.thm.cloud.integration.order.mapper;

import com.thm.cloud.integration.order.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

/**
 * 订单表 order 的 MyBatis Mapper。
 *
 * <p>{@code @Options(useGeneratedKeys=true)}：插入成功后把数据库自增主键
 * 回填到实体的 id 字段 —— OrderServiceImpl 正是靠这个拿到订单 ID 打日志。</p>
 *
 * @author thm
 */
@Mapper
@Repository
public interface OrderMapper {

	/**
	 * 插入订单记录。
	 * @param order 订单实体（插入后 id 被回填）
	 * @return 插入行数
	 */
	@Insert("INSERT INTO `order` (user_id, commodity_code, count, money, create_time, update_time) "
			+ "VALUES (#{userId}, #{commodityCode}, #{count}, #{money}, #{createTime}, #{updateTime})")
	@Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
	int saveOrder(Order order);

}
