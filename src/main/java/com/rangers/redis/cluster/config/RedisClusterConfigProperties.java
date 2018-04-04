package com.rangers.redis.cluster.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.redis.cluster")
public class RedisClusterConfigProperties {

	/*
	 * <li>spring.redis.cluster.nodes[0] = 127.0.0.1:7379 </li>
	 * <li>spring.redis.cluster.nodes[1] = 127.0.0.1:7380 ...</li>
	 */
	private List<String> nodes;

	public List<String> getNodes() {
		return nodes;
	}

	public void setNodes(List<String> nodes) {
		this.nodes = nodes;
	}
}
