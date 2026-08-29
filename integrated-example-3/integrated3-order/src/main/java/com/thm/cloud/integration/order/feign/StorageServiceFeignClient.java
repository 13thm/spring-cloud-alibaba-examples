package com.thm.cloud.integration.order.feign;

import com.thm.cloud.integration.common.Result;
import com.thm.cloud.integration.order.feign.dto.StorageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 库存服务的 Feign 客户端（订单 → 库存）。
 *
 * <p>用法同 {@link AccountServiceFeignClient}：按服务名从 Nacos 发现实例，
 * 声明式调用 /storage/reduce-stock 扣减库存，Seata 自动透传 XID。</p>
 *
 * @author thm
 */
@FeignClient(name = "integrated3-storage")
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
