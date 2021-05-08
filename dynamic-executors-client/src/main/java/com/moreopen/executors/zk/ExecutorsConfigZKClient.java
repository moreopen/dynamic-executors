package com.moreopen.executors.zk;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorsConfigZKClient {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * zk 的连接 URL 参数 ip:port,ip:port 格式
	 */
	private String connectionString;
	
	/**
	 * zk 会话超时时间，默认 5000ms
	 */
	private int sessionTimeoutMs = 5000;
	
	/**
	 * zk 重连最大次数
	 */
	private int retryTimes = 100;
	
	private CuratorFramework client;
	
	private NodeCache nodeCache;
	
	private AtomicBoolean nodeCacheInited = new AtomicBoolean(false);
	
	/**
	 * XXX 重要配置
	 * 挂载配置的根路径
	 * 如果依赖 tron, 则默认取 tron 上固定节点为根路径（遵循 torn 的配置规则）
	 * 	反之，使用方根据实际需要设置
	 */
	private String rootNode = "/moconfig/dynamic-executors/";
	
	/**
	 * 初始化方法，用于连接 ZK 并创建 client 是咧
	 */
	public void init () {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, retryTimes, 60000);
		client = CuratorFrameworkFactory.builder().connectString(connectionString)
				.retryPolicy(retryPolicy).sessionTimeoutMs(sessionTimeoutMs).build();
		client.start();
	}
	
	/**
	 * 仅调用一次
	 */
	public void watch(String appNodeName, NodeCacheListener listener) {
		if (nodeCacheInited.compareAndSet(false, true)) {
			try {
				nodeCache = new NodeCache(client, getAppNodeFullPath(appNodeName), false);
				nodeCache.start();
				nodeCache.getListenable().addListener(listener);
				logger.info("======== add watch to [" + getAppNodeFullPath(appNodeName) + "] ok");
			} catch (Exception e) {
				logger.error("======== add watch to [" + getAppNodeFullPath(appNodeName) + "] exception", e);
			}
		}
	}

	public String getConfig(String appNodeName) throws Exception {
		return new String(client.getData().forPath(getAppNodeFullPath(appNodeName)));
	}

	private String getAppNodeFullPath(String appNodeName) {
		return rootNode + appNodeName;
	}

	public void setRootNode(String rootNode) {
		this.rootNode = rootNode;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	public void setSessionTimeoutMs(int sessionTimeoutMs) {
		this.sessionTimeoutMs = sessionTimeoutMs;
	}

}
