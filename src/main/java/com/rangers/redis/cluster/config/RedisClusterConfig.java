package com.rangers.redis.cluster.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisClusterConfig {

	@Autowired
	RedisClusterConfigProperties clusterProperties;

	public @Bean RedisConnectionFactory connectionFactory() {

		List<String> nodes = clusterProperties.getNodes();
		System.out.println("配置的节点信息:" + nodes);
		return new JedisConnectionFactory(new RedisClusterConfiguration(nodes));
	}

	public @Bean RedisTemplate<String, Object> getRedisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new JdkSerializationRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new JdkSerializationRedisSerializer());
		return template;
	}

	/**
	 * jedis客户端方式
	 * 
	 * @return
	 * @author fuhw/vencano
	 */
	// public @Bean JedisCluster getJedisCluster() {
	// Set<HostAndPort> nodes = new HashSet<>();
	// for (String node : clusterProperties.getNodes()) {
	// String[] parts = StringUtils.split(node, ":");
	// Assert.state(parts.length == 2,
	// "redis node shoule be defined as 'host:port', not '" + Arrays.toString(parts)
	// + "'");
	// nodes.add(new HostAndPort(parts[0], Integer.valueOf(parts[1])));
	// }
	// return new JedisCluster(nodes);
	// }
}