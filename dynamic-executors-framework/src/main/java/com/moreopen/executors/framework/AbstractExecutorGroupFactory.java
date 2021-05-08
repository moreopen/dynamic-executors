package com.moreopen.executors.framework;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;

/**
 * 创建动态线程池组的抽象类，封装通用的代码逻辑
 */
public abstract class AbstractExecutorGroupFactory implements ExecutorGroupFactory {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected ExecutorGroup executorPool = new ExecutorGroup();
	
	protected boolean inited = false;
	
	@Override
	public ExecutorGroup create() {
		if (!inited) {
			throw new IllegalStateException("============ ExecutorGroup is not inited, plz check");
		}
		return executorPool;
	}
	/**
	 * 根据 config 内容解析成 {@see ExecutorConfig} 对象
	 * 	 config 的格式必须是 json 数组
	 */
	protected void parseConfig(String config) {
		logger.info("========== config : " + config);
		List<ExecutorConfig> executorConfigs = JSONObject.parseArray(config, ExecutorConfig.class);
		for (ExecutorConfig executorConfig : executorConfigs) {
			if (validate(executorConfig)) {
				EnhancedThreadPoolExecutor executor = (EnhancedThreadPoolExecutor) executorPool.get(executorConfig.getExecutorName());
				if (executor == null) {
					executor = new EnhancedThreadPoolExecutor(
							executorConfig.getCoreSize(),
							executorConfig.getMaxSize(),
							executorConfig.getKeepAliveSeconds(),
							executorConfig.newWorkQueue(),
							executorConfig.newThreadFactory(),
							executorConfig.newRejectedExecutionHandler());
					executor.setName(executorConfig.getExecutorName());
					executor.setMonitorEnable(executorConfig.isMonitorEnable());
					executor.setMonitorCodes(executorConfig.getMonitorCodes());
					executorPool.add(executor);
					logger.info("========== create executor succeed, name :" 
									+ executorConfig.getExecutorName() + " ," + executor.toString());
				} else {
					//update executor， XXX queueSize 暂不支持动态修改
					executor.update(executorConfig.getCoreSize(),
							executorConfig.getMaxSize(),
							executorConfig.getKeepAliveSeconds(),
							executorConfig.getQueueSize(),
							executorConfig.newThreadFactory(),
							executorConfig.newRejectedExecutionHandler(), 
							executorConfig.isMonitorEnable(), executorConfig.getMonitorCodes());
					logger.info("========== update executor succeed (Queue[type&size] is ignore!!!), name :"
									+ executorConfig.getExecutorName() + " ," + executor.toString());
				}
			}
		}
		logger.info("========== parse config and create/update all executors finished");
	}
	
	/**
	 * check executor config is valid or not
	 * @param executorConfig
	 * @return
	 */
	private boolean validate(ExecutorConfig executorConfig) {
		Assert.isTrue(StringUtils.isNotBlank(executorConfig.getExecutorName()), "executor name can't be empty");
		if (executorConfig.getCoreSize() <= 0) {
			logger.error(String.format(
					"executor [%s] coreSize [%s] is invalid, the value must > 0",
					executorConfig.getExecutorName(), executorConfig.getCoreSize())
			);
			return false;
		}
		if (executorConfig.getMaxSize() < executorConfig.getCoreSize()) {
			logger.error(String.format("executor [%s] maxSize [%s] is invalid, the value must >= coreSize [%s]", 
					executorConfig.getExecutorName(), executorConfig.getMaxSize(), executorConfig.getCoreSize())
			);
			return false;
		}
		if (!ExecutorConfig.isValidQueueType(executorConfig.getQueueType())) {
			logger.error(String.format("executor [%s] queueType [%s] is invalid, must be [%s]", 
					executorConfig.getExecutorName(), executorConfig.getQueueType(), ExecutorConfig.QUEUE_TYPES));
			return false;
		}
		if (executorConfig.getQueueSize() <= 0) {
			logger.info(String.format("executor [%s] queueSize [%s], reset to Integer.MAX_VALUE",
					executorConfig.getExecutorName(), executorConfig.getQueueSize()));
			executorConfig.setQueueSize(Integer.MAX_VALUE);
		}
		if (executorConfig.getKeepAliveSeconds() < 0) {
			logger.error(String.format("executor [%s] keepAliveSeconds [%s] is invalid, the value must >= 0", 
					executorConfig.getExecutorName(), executorConfig.getKeepAliveSeconds()));
			return false;
		}
		if (StringUtils.isNotBlank(executorConfig.getRejectHandler()) 
				&& !ExecutorConfig.isValidRejectHandler(executorConfig.getRejectHandler())) {
			logger.error(String.format("executor [%s] rejectHandler [%s] is invalid, must be [%s]", 
					executorConfig.getExecutorName(), executorConfig.getRejectHandler(), ExecutorConfig.REJECT_HANDLERS));
			return false;
		}
		return true;
	}
	
	/**
	 * XXX 完成 ExecutorGroup 的构建
	 *  	使用 ExecutorGroup 之前必须调用 init 方法完成初始化并设置 inited 参数为 true
	 */
	protected abstract void init() throws Exception;

}
