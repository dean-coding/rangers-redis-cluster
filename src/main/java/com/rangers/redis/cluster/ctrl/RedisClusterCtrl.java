package com.rangers.redis.cluster.ctrl;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("redis")
public class RedisClusterCtrl implements InitializingBean {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private final static String REDIS_CLUSTER_DEMO_LIST_KEY = "REDIS_CLUSTER_DEMO_LIST_KEY";

	private BoundListOperations<String, Object> boundListOps;

	@Override
	public void afterPropertiesSet() throws Exception {
		boundListOps = redisTemplate.boundListOps(REDIS_CLUSTER_DEMO_LIST_KEY);
		Assert.notNull(boundListOps, "can not get boundListOps from redisTemplate");
	}

	@GetMapping("/put-left")
	public void putLeft(@RequestParam("leftVal") String leftVal) {
		boundListOps.leftPush(leftVal);
	}

	@GetMapping("/put-right")
	public void putRight(@RequestParam("rightVal") String rightVal) {
		boundListOps.rightPush(rightVal);
	}

	@GetMapping("range")
	public List<Object> get(@RequestParam(name = "start", required = false,defaultValue="0") Long start,
			@RequestParam(name = "end", required = true) Long end) {
		return boundListOps.range(start, end);
	}
}
