package com.moreopen.executors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.moreopen.executors.framework.AbstractExecutorGroupFactory;

/**
 * 基于本地配置文件，实现动态线程组的工厂类
 * 	支持定时刷新指定的配置文件，动态更新线程组核心参数
 */
public class LocalConfigExecutorGroupFactory extends AbstractExecutorGroupFactory {
	
	private String localFile;
	
	private long modifiedTime = 0;
	
	private File localFileObject = null;
	
	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	
	@Override
	public void init() throws Exception {
		Assert.isTrue(StringUtils.isNotEmpty(localFile));
		logger.info("========== localFile :" + localFile);
		if (localFile.startsWith("/") || localFile.contains(":" + File.separator)) {
			//文件是绝对路径
			localFileObject = new File(localFile);
		} else {
			String classResourceFilePath = getClass().getResource("/").getFile() + localFile;
			logger.info(String.format("======= classResourceFilePath [%s]", classResourceFilePath));
			localFileObject = new File(classResourceFilePath);
		}
		if (!localFileObject.exists()) {
			throw new FileNotFoundException("local file not found : " + localFileObject.getAbsolutePath());
		}
		modifiedTime = localFileObject.lastModified();
		readAndParse();
		
		//设置初始化标识
		this.inited = true;
		
		//定期检查配置文件是否发生变更，并刷新对应的线程池对象
		executorService.scheduleWithFixedDelay(new RefreshTask(), 10, 5, TimeUnit.SECONDS);
	}

	private void readAndParse() throws IOException {
//		InputStream resourceAsStream = getClass().getResourceAsStream(localFile);
		InputStream resourceAsStream = null;
		try {
			resourceAsStream = new FileInputStream(localFileObject);
			parseConfig(IOUtils.toString(resourceAsStream));
		} catch (Exception e) {
			logger.error("read and parse config exception", e);
		} finally {
			IOUtils.closeQuietly(resourceAsStream);
		}
	}

	public void setLocalFile(String localFile) {
		this.localFile = localFile;
	}
	
	class RefreshTask implements Runnable {
		@Override
		public void run() {
			long ts = localFileObject.lastModified();
			if (ts > modifiedTime) {
				try {
					readAndParse();
					modifiedTime = ts;
					logger.info(String.format("local config file [%s] is modified, read and reload executors", localFile));
				} catch (IOException e) {
					logger.error("read and parse config exception", e);
				}
			}
		}
		
	}
}
