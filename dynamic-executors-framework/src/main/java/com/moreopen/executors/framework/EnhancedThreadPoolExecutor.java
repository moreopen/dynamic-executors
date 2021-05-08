package com.moreopen.executors.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.moreopen.executors.framework.policy.CountableRejectedExecutionHandler;

/**
 * 自定义的增强型线程池，增加监控、动态调参等辅助功能
 * 		核心的任务执行、线程调度直接复用原有的线程池功能
 */
public class EnhancedThreadPoolExecutor extends ThreadPoolExecutor {
	
	private String name;
	
	private boolean monitorEnable = true;
	
	private List<String> monitorCodes = new ArrayList<String>();

	public EnhancedThreadPoolExecutor(
			int corePoolSize, 
			int maximumPoolSize, 
			long keepAliveTime, 
			BlockingQueue<Runnable> workQueue, 
			ThreadFactory threadFactory, 
			RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue, threadFactory, handler);
	}
	
	public void update(
			int coreSize, 
			int maxSize, 
			int keepAliveSeconds, 
			int queueSize, 
			ThreadFactory threadFactory,
			RejectedExecutionHandler rejectedExecutionHandler,
			boolean monitorEnable,
			List<String> monitorCodes) {
		setCorePoolSize(coreSize);
		setMaximumPoolSize(maxSize);
		setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
		setThreadFactory(threadFactory);
		setRejectedExecutionHandler(rejectedExecutionHandler);
		setMonitorEnable(monitorEnable);
		setMonitorCodes(monitorCodes);
	}
	
	@Override
	public void execute(Runnable command) {
		super.execute(command);
	}
	
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return super.submit(task);
	}
	
	@Override
	public Future<?> submit(Runnable task) {
		return submit(task);
	}
	
	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return submit(task, result);
	}
	
	public ExecutorStats stats() {
		int activeThreadLoad = getActiveCount() * 100 / getMaximumPoolSize();
		int taskCompletionRate = 
				getTaskCount() == 0 ? 0
						: (int) (getCompletedTaskCount() * 100l / getTaskCount());
		int queueSize = getQueue().size();
		long rejectedCount = ((CountableRejectedExecutionHandler) getRejectedExecutionHandler()).count();
		return new ExecutorStats(activeThreadLoad, taskCompletionRate, queueSize, rejectedCount);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMonitorEnable() {
		return monitorEnable;
	}

	public void setMonitorEnable(boolean monitorEnable) {
		this.monitorEnable = monitorEnable;
	}

	public List<String> getMonitorCodes() {
		return monitorCodes;
	}

	public void setMonitorCodes(List<String> monitorCodes) {
		this.monitorCodes = monitorCodes;
	}
	
	/**
	 * Executor 线程池的运行状态
	 */
	public static class ExecutorStats {
		
		private int activeThreadLoad;
		
		private int taskCompletionRate;
		
		private int queueSize;
		
		private long rejectedCount;
		
		public ExecutorStats() {
			//default constuctor
		}

		public ExecutorStats(int activeThreadLoad, int taskCompletionRate, int queueSize, long rejectedCount) {
			this.activeThreadLoad = activeThreadLoad;
			this.taskCompletionRate = taskCompletionRate;
			this.queueSize = queueSize;
			this.rejectedCount = rejectedCount;
		}

		public int getActiveThreadLoad() {
			return activeThreadLoad;
		}

		public void setActiveThreadLoad(int activeThreadLoad) {
			this.activeThreadLoad = activeThreadLoad;
		}

		public int getTaskCompletionRate() {
			return taskCompletionRate;
		}

		public void setTaskCompletionRate(int taskCompletionRate) {
			this.taskCompletionRate = taskCompletionRate;
		}

		public int getQueueSize() {
			return queueSize;
		}

		public void setQueueSize(int queueSize) {
			this.queueSize = queueSize;
		}

		public long getRejectedCount() {
			return rejectedCount;
		}

		public void setRejectedCount(long rejectedCount) {
			this.rejectedCount = rejectedCount;
		}
		
		
	}

}
