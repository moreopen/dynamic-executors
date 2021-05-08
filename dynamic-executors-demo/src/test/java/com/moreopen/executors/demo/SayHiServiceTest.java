package com.moreopen.executors.demo;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.util.Assert;

import com.moreopen.executors.demo.SayHiService;

@ContextConfiguration(locations = {"classpath:app.startup.xml"})
public class SayHiServiceTest extends AbstractJUnit4SpringContextTests {
	
	@Autowired
	private SayHiService sayHiService;
	
	private int taskNum = 10000;
	
	@Before
	public void before() {
		Assert.notNull(sayHiService);
	}

	@Test
	public void test() throws InterruptedException {
		for (int i = 0; i< taskNum; i++) {
			sayHiService.sayHi();			
		}
		Thread.currentThread().join();
	}

}
