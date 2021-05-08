package com.moreopen.executors.client;

import java.io.File;
import java.util.concurrent.Executor;

import org.junit.Test;
import org.springframework.util.Assert;

import com.moreopen.executors.LocalConfigExecutorGroupFactory;
import com.moreopen.executors.ZKConfigExecutorGroupFactory;
import com.moreopen.executors.framework.ExecutorGroup;
import com.moreopen.executors.zk.ExecutorsConfigZKClient;

public class ExecutorFactoryTest {
	
	private String localFile = "executors.conf";
	
	private String connectionString = "test-dubbo-zk-01.im.int:2181,test-dubbo-zk-01.im.int:2181,test-dubbo-zk-01.im.int:2181";


	@Test
	public void testGetExecutorBasedLocalConfig() throws Exception {
		
		LocalConfigExecutorGroupFactory executorFactory = new LocalConfigExecutorGroupFactory();
		executorFactory.setLocalFile(localFile);
		executorFactory.init();
		ExecutorGroup executorPool = executorFactory.create();
		Executor executor = executorPool.get("sync-executor");
		Assert.notNull(executor);
		System.out.println(executor);
	}
	
	@Test
	public void testGetExecutorBasedZKConfig() throws Exception {
		ExecutorsConfigZKClient zkClient = new ExecutorsConfigZKClient();
		zkClient.setConnectionString(connectionString);
		zkClient.init();
		
		ZKConfigExecutorGroupFactory executorPoolFactory = new ZKConfigExecutorGroupFactory();
		executorPoolFactory.setZkClient(zkClient);
		executorPoolFactory.setAppNodeName("sayHi-demo");
		executorPoolFactory.init();
		
		ExecutorGroup executorPool = executorPoolFactory.create();
		Executor executor = executorPool.get("sync-executor");
		Assert.notNull(executor);
		System.out.println(executor);
	}
	
	public static void main(String[] args) {
		String str = "/Users/yk/sg-im/dynamic-executors/dynamic-executors-client/target/classes/com/moreopen/executors/client/executors.conf";
		File file = new File(str);
		System.out.println("========== file : " + file);
		System.out.println(file.exists());
	}

	
}
