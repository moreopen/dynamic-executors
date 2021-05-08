package com.moreopen.executors.framework.policy;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于统计 RejectedExecutionHandler 触发次数 （Decorate 模式）
 * 	Reject 策略完全取决于注入的 executionHander 实例
 */
public class CountableRejectedExecutionHandler implements RejectedExecutionHandler, Countable {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private RejectedExecutionHandler executionHandler;
	
	/**
	 * 值读取一次就被清零，重新计数（只用于监控分时上报）
	 */
	private AtomicLong counter = new AtomicLong(0);
	
	public CountableRejectedExecutionHandler(RejectedExecutionHandler handler) {
		this.executionHandler = handler;
	}
	
	@Override
	public long count() {
		return counter.getAndSet(0);
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		try {
			counter.incrementAndGet();
			executionHandler.rejectedExecution(r, executor);
		} catch (Exception e) {
			logger.error("rejectedExecution occurred", e);
		}
	}

}
