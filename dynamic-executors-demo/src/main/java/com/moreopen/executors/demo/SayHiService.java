package com.moreopen.executors.demo;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.moreopen.executors.framework.ExecutorGroup;

@Service
public class SayHiService {
	@Resource
	private ExecutorGroup executorGroup;
	
	private Executor executor; 
	
	@PostConstruct
	public void init() {
		//executor = Executors.newFixedThreadPool(10); // Deperated !!!
		executor = executorGroup.get("sayHi");
		Assert.notNull(executor);
	}
	
	public void sayHi() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				System.out.println("hi");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
