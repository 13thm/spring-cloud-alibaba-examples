package com.thm.cloud.integration.order.feign;

import com.thm.cloud.integration.common.Result;
import com.thm.cloud.integration.order.feign.dto.AccountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 账户服务的 Feign 客户端（订单 → 账户）。
 *
 * <p>Feign 声明式调用：只需按目标服务的 HTTP 接口"抄"一份方法签名，
 * Spring 会生成代理对象，调用时自动完成：</p>
 * <ol>
 *   <li>从 Nacos 拉取 integrated2-account 的实例列表并负载均衡选址；</li>
 *   <li>把方法参数序列化为 JSON 并发起 HTTP POST；</li>
 *   <li>把响应反序列化为 Result 返回；</li>
 *   <li>（配合 Seata）在请求头中透传全局事务 XID —— 跨服务事务传播的关键。</li>
 * </ol>
 *
 * <p>注意：方法路径/参数必须与 AccountController 的接口完全一致，否则 404；
 * name 属性即 Nacos 注册的服务名。</p>
 *
 * @author thm
 */
@FeignClient(name = "integrated3-account")
public interface AccountServiceFeignClient {

	/**
	 * 调用账户服务扣减余额。
	 * 对应 AccountController#reduceBalance。
	 * @param accountReduceBalanceDTO 扣减请求（userId + price）
	 * @return 统一响应，code=2003 表示余额不足等业务失败
	 */
	@PostMapping("/account/reduce-balance")
	Result<?> reduceBalance(@RequestBody AccountDTO accountReduceBalanceDTO);

}
