package com.moreopen.executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.springframework.util.Assert;

import com.moreopen.executors.framework.AbstractExecutorGroupFactory;
import com.moreopen.executors.zk.ExecutorsConfigZKClient;

/**
 * 基于 ZooKeeper 配置实现动态线程池组工厂类
 * 		通过 Zookeeper 加载配置，并监听配置变更动态更新线程池参数
 */
public class ZKConfigExecutorGroupFactory extends AbstractExecutorGroupFactory {
	
	/**
	 * zookeeper 上用于存放配置信息的节点名称
	 */
	private String appNodeName;
	
	private ExecutorsConfigZKClient zkClient;
	
	@Override
	public void init() throws Exception {
		Assert.isTrue(StringUtils.isNotBlank(appNodeName));
		try {
			String config = zkClient.getConfig(appNodeName);
			parseConfig(config);
			//设置初始化标识
			this.inited = true;
			
			//增加对 appNode 值发生变化的监听器
			zkClient.watch(appNodeName, new ConfigChangedListener());
		} catch (Exception e) {
			throw new RuntimeException("========== get config or parse exception", e);
		}
	}

	public void setZkClient(ExecutorsConfigZKClient zkClient) {
		this.zkClient = zkClient;
	}

	public void setAppNodeName(String appNodeName) {
		this.appNodeName = appNodeName;
	}
	
	class ConfigChangedListener implements NodeCacheListener {
		@Override
		public void nodeChanged() throws Exception {
			String value = zkClient.getConfig(appNodeName);
			logger.info(String.format("========= config [%s] value is changed, new value [%s]", appNodeName, value));
			parseConfig(value);
		}
		
	}

}
	