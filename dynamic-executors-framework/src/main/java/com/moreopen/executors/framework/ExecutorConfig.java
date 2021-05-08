package com.moreopen.executors.framework;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.moreopen.executors.framework.policy.CountableRejectedExecutionHandler;

/**
 * 创建线程池的核心参数配置
 * {
		"executorName":"sync线程池",
		"coreSize":1,
		"maxSize":10,
		"keepAliveSeconds":120,
		"queueType":"LinkedBlockingQueue",
		"queueSize":1000,
		"rejectHandler":"AbortPolicy",
		"threadNamePrefix":"sync-",
		"monitorCodes":["工作线程负荷", "任务的完成率", "队列中的任务数", "reject数量"]
	}
 */
public class ExecutorConfig {
	
	/**
	 * 不支持 DelayedWorkQueue
	 * 	DelayedWorkQueue is used for ScheduledThreadPoolExecutor，一般不会处理实时大并发的请求调用
	 */
	public static final List<String> QUEUE_TYPES =
			Arrays.asList("LinkedBlockingQueue", "SynchronousQueue");

	/**
	 * default handler is AbortPolicy
	 */
	public static final List<String> REJECT_HANDLERS = 
			Arrays.asList("AbortPolicy", "CallerRunsPolicy", "DiscardOldestPolicy", "DiscardPolicy");

	private String executorName;
	
	private int coreSize;
	
	private int maxSize;
	
	private int keepAliveSeconds;
	
	private String queueType;
	
	private int queueSize;
	
	private String rejectHandler;
	
	private String threadNamePrefix;
	
	private List<String> monitorCodes;
	
	private boolean monitorEnable = true;

	public String getExecutorName() {
		return executorName;
	}

	public void setExecutorName(String executorName) {
		this.executorName = executorName;
	}

	public int getCoreSize() {
		return coreSize;
	}

	public void setCoreSize(int coreSize) {
		this.coreSize = coreSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getKeepAliveSeconds() {
		return keepAliveSeconds;
	}

	public void setKeepAliveSeconds(int keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}

	public String getQueueType() {
		return queueType;
	}

	public void setQueueType(String queueType) {
		this.queueType = queueType;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public String getRejectHandler() {
		return rejectHandler;
	}

	public void setRejectHandler(String rejectHandler) {
		this.rejectHandler = rejectHandler;
	}

	public String getThreadNamePrefix() {
		return threadNamePrefix;
	}

	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}

	public List<String> getMonitorCodes() {
		return monitorCodes;
	}

	public void setMonitorCodes(List<String> monitorCodes) {
		this.monitorCodes = monitorCodes;
	}

	public static boolean isValidQueueType(String queueType) {
		return QUEUE_TYPES.contains(queueType);
	}

	public static boolean isValidRejectHandler(String rejectHandler) {
		return REJECT_HANDLERS.contains(rejectHandler);
	}

	public BlockingQueue<Runnable> newWorkQueue() {
		if ("LinkedBlockingQueue".equalsIgnoreCase(queueType)) {
			return new LinkedBlockingQueue<>(queueSize);
		} else if ("SynchronousQueue".equalsIgnoreCase(queueType)) {
			return new SynchronousQueue<>();
		}
		throw new IllegalArgumentException("unsupported queue type : " + queueType);
	}

	public ThreadFactory newThreadFactory() {
		if (org.apache.commons.lang3.StringUtils.isNotBlank(threadNamePrefix)) {
			return new CustomizableThreadFactory(threadNamePrefix);
		}
		return null;
	}

	/**
	 * XXX TODO 后续支持自定义 RejectHandler
	 */
	public RejectedExecutionHandler newRejectedExecutionHandler() {
		if ("AbortPolicy".equalsIgnoreCase(rejectHandler)) {
			return new CountableRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		}
		if ("CallerRunsPolicy".equalsIgnoreCase(rejectHandler)) {
			return new CountableRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		}
		if ("DiscardOldestPolicy".equalsIgnoreCase(rejectHandler)) {
			return new CountableRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
		}
		if ("DiscardPolicy".equals(rejectHandler)) {
			return new CountableRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
		}
		throw new IllegalArgumentException("unsupported reject handler : " + rejectHandler);
	}

	public boolean isMonitorEnable() {
		return monitorEnable;
	}

	public void setMonitorEnable(boolean monitorEnable) {
		this.monitorEnable = monitorEnable;
	}

}
