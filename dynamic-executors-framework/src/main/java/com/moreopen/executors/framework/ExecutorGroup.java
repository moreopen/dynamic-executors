package com.moreopen.executors.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 动态线程池的容器, 维护和管理所有根据配置创建的线程池
 */
public class ExecutorGroup {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private List<EnhancedThreadPoolExecutor> executors = new ArrayList<EnhancedThreadPoolExecutor>();
	
	/**
	 * 根据线程池名字返回对应的线程池对象
	 */
	public Executor get(String name) {
		for (EnhancedThreadPoolExecutor executor : executors) {
			if (executor.getName().equalsIgnoreCase(name)) {
				return executor;
			}
		}
		logger.info("can't find executor by name :" + name);
		return null;
	}

	public void add(EnhancedThreadPoolExecutor executor) {
		this.executors.add(executor);
	}
	
	public List<EnhancedThreadPoolExecutor> all() {
		return executors;
	}

}
