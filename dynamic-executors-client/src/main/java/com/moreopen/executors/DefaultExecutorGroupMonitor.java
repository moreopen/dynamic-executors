package com.moreopen.executors;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.moreopen.executors.framework.EnhancedThreadPoolExecutor;
import com.moreopen.executors.framework.ExecutorGroup;
import com.moreopen.executors.framework.ExecutorGroupMonitor;


/**
 * 默认的监控实现类，定期上报 {@see EnhancedThreadPoolExecutor.ExecutorStats 数据到 monitor 平台}
 * 	XXX 使用方可以自定义监控实现
 */
public class DefaultExecutorGroupMonitor implements ExecutorGroupMonitor {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * "monitorCodes":["工作线程负荷", "任务的完成率", "队列中的任务数", "reject数量"]
	 * 目前只支持以上四个维度的监控
	 */
	private ExecutorGroup executorGroup;
	
	/**
	 * 启动定时上报监控数据
	 */
	public void start() {
		Assert.notNull(executorGroup);
		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					monitor();
				} catch (Exception e) {
					logger.error("monitor executor error", e);
				}
			}
		}, 10, 5, TimeUnit.SECONDS);
	}
	
	@SuppressWarnings("unused")
	@Override
	public void monitor() {
		List<EnhancedThreadPoolExecutor> executors = executorGroup.all();
		for (EnhancedThreadPoolExecutor executor : executors) {
			if (executor.isMonitorEnable() && CollectionUtils.isNotEmpty(executor.getMonitorCodes())) {
				EnhancedThreadPoolExecutor.ExecutorStats stats = executor.stats();
				logger.info(String.format(
						"============ Monitor Executor[%s], activeThreadLoad[%s], taskCompletionRate[%s], queueSize[%s], rejectedCount[%s]",
						executor.getName(), stats.getActiveThreadLoad(), stats.getTaskCompletionRate(),
						stats.getQueueSize(), stats.getRejectedCount()));
				List<String> monitorCodes = executor.getMonitorCodes();
				//TODO 上报监控项
			}
			
		}
	}

	public void setExecutorGroup(ExecutorGroup executorGroup) {
		this.executorGroup = executorGroup;
	}

}
