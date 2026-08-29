package com.thm.cloud.integration.storage.controller;

import com.thm.cloud.integration.common.BusinessException;
import com.thm.cloud.integration.common.Result;
import com.thm.cloud.integration.storage.dto.StorageDTO;
import com.thm.cloud.integration.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 库存服务 HTTP 接口。
 *
 * <p>两个入口：</p>
 * <ul>
 *   <li>POST /storage/reduce-stock —— 扣减库存，被订单服务 Feign 调用（分布式事务链路）；</li>
 *   <li>GET  /storage/?commodityCode=x —— 查询剩余库存，测试用。</li>
 * </ul>
 *
 * @author thm
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
	 * 转成 code=2003 的失败 Result 返回 —— 此时 HTTP 层面是 200 成功响应，
	 * 调用方（订单服务）通过判断 Result.code 得知失败后自己抛异常触发全局回滚。</p>
	 */
	@PostMapping("/reduce-stock")
	public Result<?> reduceStock(@RequestBody StorageDTO storageDTO) {
		try {
			storageService.reduceStock(storageDTO.getCommodityCode(), storageDTO.getCount());
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
