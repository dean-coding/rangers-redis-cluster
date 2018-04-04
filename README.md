# springboot redis cluster

本示例使用ListOperation演示的结果:

http://localhost:8080/redis/put-left?leftVal=***
http://localhost:8080/redis/put-right?rightVal=***

http://localhost:8080/redis/range?end=10 //查询结果:

	[
	"can you tel me what's your name ?",
	"my name is xiaolang",
	"hello world",
	"i am a val from right"
	]

# 项目配置:

## 1.依赖

	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-data-redis</artifactId>
	</dependency>
## 2.propertis

yml配置

	spring:
	  redis:
	    cluster:
	      nodes:
	      - 127.0.0.1:7031
	      - 127.0.0.1:7032
	      - 127.0.0.1:7033
	      - 127.0.0.1:7034
	      - 127.0.0.1:7035
	      - 127.0.0.1:7036
	
RedisClusterConfigProperties	
	      
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
	
## 3.RedisClusterConfig 的配置
	
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
	
# redis cluster 配置;

使用场景:

	redis是个内存数据库，数据都存在内存中，既然存在内存中，那么大小肯定受服务器内存大小的限制。比如一个64G内存的服务器，一个redis撑死也就能存储64G的数据量，
	而对于大型网站架构，数据量岂止是64G，有的网站甚至存储了上T的数据量，遇见这种情况，怎么办？

      在redis 3.0版本之前，通常是获取key的hashcode，然后取模（mod），但是这种做法的缺点是无法很好的支持动态伸缩要求，一但有节点的增加或者删除操作，都会导致key
    无法在redis上命中。redis 3.0版本之后开始支持集群（cluster），采用的是哈希槽（hash slot）。CRC16(K)%16384算法(memecached采用的一致性hash算法)决定key
    对应的slot槽,他可以把多个redis实例整合在一起，形成一个集群。比如100G的数据量，一台服务器存储不下，那我可以将它分散到多台机器上，每个客户端都连接一个redis服
    务实例，这是一个无中心结构，每个节点 都保存数据和整个集群的状态。每个节点也都知道其他节点所负责的槽。
      取值的流程:传入的key -> CRC16(K)%16384 决定进入哪个slot槽,slot槽属于哪个node节点,若果不在当前node上,当前node知道改slot对应的node会Redirect过去;
    

可以选择docker 安装配置
可以手动安装配置cluster


redis.conf修改的配置项:
		
	（1）绑定端口，port 7031
	（2）指定数据存放路径，dir /usr/local/redis-cluster/7031
	（3）启动集群模式，cluster-enabled yes
	（4）后台启动，daemonize yes
	（5）指定集群节点配置文件，cluster-config-file nodes-7031.conf
	（6）指定集群节点超时时间，cluster-node-timeout 5000
	（7）指定持久化方式，appendonly yes
	（8）绑定IP，bind 127.0.0.1
	

集群结构:

	
	| 文件夹                | 端口           |  节点类型        |  分片信息		 |
	| ---------------------| -----------------------------------------------------|
	| 7031                 | 7031          |   master            |                     |
	| 7032                 | 7032          |   master             |                     |
	| 7033                 | 7033          |   master             |                     |
	| 7034                 | 7034          |   slave             |                     |
	| 7035                 | 7035          |   slave              |                     |
	| 7036                 | 7036          |   slave              |                     |
	| 7037                 | 7037          | master动态添加/移除      |                     |

目录结构:
	
	-- /usr/local/redis-cluster
		-- 7031
		-- 7032 
		-- 7033
		-- 7034 
		-- 7035 
		-- 7036
		-- redis-3.2.11
		-- .new-node.sh
		-- .start-cluster.sh
		-- .shutdown-cluster.sh
		-- redis.conf.demo
		-- REMAME.md

## 1.去官网下载redis,或包管理器:yum/gem/brew等等
## 2.进入终端: /usr/local/redis-cluster (创建redis-cluster目录)
## 3.解压 redis包到 /usr/local/redis-cluster/redis-3.2.11
## 4.sudo cp /redis-3.2.11 中的redis.conf -> redis.conf.demo (修改上述提到的配置项作为配置模版)
## 5.创建目录和配置
sudo mkdir -p /usr/local/redis-cluster/7037 && cp ./redis.conf.demo /usr/local/redis-cluster/7037/redis.conf && sed -i '' 's/7030/7037/g' /usr/local/redis-cluster/7037/redis.conf   
 
(7030-》redis.conf.demo中修改的端口;7037-》指定node名,port端口名)

依次执行上述命令(也可自行创建脚本处理): 得到 7031,7032,7033,7034,7035,7036文件夹,每个文件夹对应有redis.conf配置

## 6.启动配置的node

for((i=1;i<=6;i++)); do /usr/local/redis-cluster/redis-3.2.11/src/redis-server /usr/local/redis-cluster/703$i/redis.conf; done

	      -bash-3.2$ ps -ef | grep redis.server
		    0  7866     1   0  4:58下午 ??         0:03.57 /usr/local/redis-cluster/redis-3.2.11/src/redis-server 127.0.0.1:7031 [cluster] 
		    0  7868     1   0  4:58下午 ??         0:03.55 /usr/local/redis-cluster/redis-3.2.11/src/redis-server 127.0.0.1:7032 [cluster] 
		    0  7870     1   0  4:58下午 ??         0:03.55 /usr/local/redis-cluster/redis-3.2.11/src/redis-server 127.0.0.1:7033 [cluster] 
		    0  7872     1   0  4:58下午 ??         0:03.53 /usr/local/redis-cluster/redis-3.2.11/src/redis-server 127.0.0.1:7034 [cluster] 
		    0  7874     1   0  4:58下午 ??         0:03.53 /usr/local/redis-cluster/redis-3.2.11/src/redis-server 127.0.0.1:7035 [cluster] 
		    0  7876     1   0  4:58下午 ??         0:03.52 /usr/local/redis-cluster/redis-3.2.11/src/redis-server 127.0.0.1:7036 [cluster] 
		  501  7926  7779   0  5:45下午 ttys000    0:00.00 grep redis.server
		-bash-3.2$ 

## 7.创建集群 
cd ./redis-3.2.11/src

执行创建:
./redis-trib.rb create --replicas 1 127.0.0.1:7031 127.0.0.1:7032 127.0.0.1:7033 127.0.0.1:7034 127.0.0.1:7035 127.0.0.1:7036

交互选择即可

## 8.连接测试
cd ./redis-3.2.11/src

./redis-cli -c -p 7031  (-c指定是集群连接,否则的话,会在set,get的时候报错MOVED..)
	    
	     -bash-3.2$ redis-cli -c -p 7031
		127.0.0.1:7031> get A
		-> Redirected to slot [6373] located at 127.0.0.1:7032
		"xiaolang"
		127.0.0.1:7032> get B
		"xiaohei"
		127.0.0.1:7032> get C
		-> Redirected to slot [14503] located at 127.0.0.1:7033
		"huoqiang"
		127.0.0.1:7033> 
	
set/get 可能会去不同的node(不同的slot)	
查看节点信息:
	
	      127.0.0.1:7033> cluster nodes
		6ddcd6c1970114ae881f0c0a3bdb00c56aa96b9d 127.0.0.1:7035 slave 5e1594337d8b4afdc2898588f16b4d5e2cff8936 0 1522835309965 5 connected
		8722e5a2bae76b9ef85f6b73497965fead4e106a 127.0.0.1:7033 myself,master - 0 0 3 connected 11712-16383
		a4c7c35dbf156599c1bd133e95ff3ff7d24505c0 127.0.0.1:7036 slave 8722e5a2bae76b9ef85f6b73497965fead4e106a 0 1522835310970 3 connected
		9b166a42f352fbe24732a53ce2a00ae013f2fcad 127.0.0.1:7034 slave 600b343193e410a21bbf7cc3a7fed3984db083c8 0 1522835309462 8 connected
		5e1594337d8b4afdc2898588f16b4d5e2cff8936 127.0.0.1:7032 master - 0 1522835311476 2 connected 6250-10922
		600b343193e410a21bbf7cc3a7fed3984db083c8 127.0.0.1:7031 master - 0 1522835310468 8 connected 0-6249 10923-11711
		127.0.0.1:7033> cluster info
		cluster_state:ok
		cluster_slots_assigned:16384
		cluster_slots_ok:16384
		cluster_slots_pfail:0
		cluster_slots_fail:0
		cluster_known_nodes:6
		cluster_size:3
		cluster_current_epoch:8
		cluster_my_epoch:3
		cluster_stats_messages_sent:13135
		cluster_stats_messages_received:13132
		127.0.0.1:7033> 

## redis集群的add-node,reshard,del-node等待补充
