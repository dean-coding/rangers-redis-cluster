package com.rangers.redis.cluster.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
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

	private static final Logger log = LoggerFactory.getLogger(RedisClusterConfig.class);

	@Autowired
	private RedisClusterConfigProperties clusterProperties;
	@Autowired
	private RedisProperties redisProperties;
	@Value("${redis.mode.cluster:false}")
	private Boolean redisModeCluster;

	public @Bean RedisConnectionFactory connectionFactory() {
		log.info("当前redis的连接模式是否是集群:{}", redisModeCluster);
		JedisConnectionFactory factory;
		if (redisModeCluster == null || !redisModeCluster) {
			factory = new JedisConnectionFactory();
			String host = redisProperties.getHost();
			String password = redisProperties.getPassword();
			int database = redisProperties.getDatabase();
			int port = redisProperties.getPort();
			factory.setHostName(host);
			factory.setPassword(password);
			factory.setDatabase(database);
			factory.setPort(port);
			factory.setTimeout(redisProperties.getTimeout());
			log.info("配置的连接factory配置信息:host={},password={},database={},port={}", host, password, database, port);
		} else {
			List<String> nodes = clusterProperties.getNodes();
			factory = new JedisConnectionFactory(new RedisClusterConfiguration(nodes));
			log.info("配置的连接factory配置信息:{}", nodes);
		}
		return factory;
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