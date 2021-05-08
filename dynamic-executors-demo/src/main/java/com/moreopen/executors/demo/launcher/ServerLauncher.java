package com.moreopen.executors.demo.launcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Server launcher
 *
 * @author xupeilu@snda.com 2015-1-13 上午10:11:58
 */
public class ServerLauncher {
    private static Logger logger = Logger.getLogger(ServerLauncher.class);

    private final static Map<String, String> paramMap = new HashMap<String, String>();

    private final static String SPACE = ":";

    public static void main(String[] args) throws Exception {
        // 关键参数
        String webapp = System.getProperty("webapp", "webapp");
        String port = System.getProperty("port", "8888");
        String contextPath = System.getProperty("contextPath", "/");

        paramMap.put("webapp", webapp);
        paramMap.put("port", port);
        paramMap.put("contextPath", contextPath);

        Server server = new Server();

        logger.info("====6666666====>" + System.getProperty("dubbo.shutdown.hook"));
        // 构造关键组件
        Connector connector = getConnector(Integer.parseInt(port));
        ThreadPool threadPool = getThreadPool();
        WebAppContext context = new WebAppContext(webapp, contextPath);
        Handler requestLogHandler = getRequestLogHandler();
        if (requestLogHandler != null) {
            context.setHandler(requestLogHandler);
        }
        //通过jetty访问static资源
        HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(context);

        server.addConnector(connector);
        server.setThreadPool(threadPool);
        server.setStopAtShutdown(true);
        server.setHandler(handlers);
        server.setSendServerVersion(false);
        server.start();


        if (!context.isAvailable()) {
            if (context.isThrowUnavailableOnStartupException()) {
                context.getUnavailableException().printStackTrace();
            }
            System.err.println(
                    new SimpleDateFormat("yyyy/MM/dd HH:mm:ssSSS").format(new Date())
                            + "Failed startup of WebAppContext! \n"
                            + "======== app startup failed! ========");
            System.exit(-1);
        }


        // 打印成功启动消息
	System.out.println(
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ssSSS").format(new Date())
                        + "  ======== dynamic-executors startup ok! ========");
        showServerConfiguration();

        server.join();
    }


    private static Connector getConnector(int port) {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setAcceptors(Runtime.getRuntime().availableProcessors() - 1);

        String maxIdleTime = System.getProperty("maxIdleTime", "3000");
        String lowResourcesMaxIdleTime = System.getProperty("lowResourcesMaxIdleTime", "2000");
        String acceptQueueSize = System.getProperty("acceptQueueSize", "100");

        paramMap.put("maxIdleTime", maxIdleTime);
        paramMap.put("lowResourcesMaxIdleTime", lowResourcesMaxIdleTime);
        paramMap.put("acceptQueueSize", acceptQueueSize);

        // 连接最大超时时间(sec)
        connector.setMaxIdleTime(Integer.parseInt(maxIdleTime));

        // 当线程资源紧张时，连接的最大超时时间 (sec)
        connector.setLowResourcesMaxIdleTime(Integer.parseInt(lowResourcesMaxIdleTime));

        // backlog，等待连接的数目
        connector.setAcceptQueueSize(Integer.parseInt(acceptQueueSize));

        return connector;
    }

    private static ThreadPool getThreadPool() {
        String _corePoolSize = System.getProperty("corePoolSize", "10");
        String _maxPoolSize = System.getProperty("maxPoolSize", "30");
        String _poolQueueSize = System.getProperty("poolQueueSize", "50");

        paramMap.put("corePoolSize", _corePoolSize);
        paramMap.put("maxPoolSize", _maxPoolSize);
        paramMap.put("poolQueueSize", _poolQueueSize);

        int corePoolSize = Integer.parseInt(_corePoolSize);
        int maxPoolSize = Integer.parseInt(_maxPoolSize);
        int poolQueueSize = Integer.parseInt(_poolQueueSize);

        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(poolQueueSize);
        return new ExecutorThreadPool(corePoolSize, maxPoolSize, 60, TimeUnit.SECONDS, queue);
    }

    private static Handler getRequestLogHandler() {

        String enableAccessLog = System.getProperty("enableAccessLog", "true");
        String accessLogPath = System.getProperty("accessLogPath", ".");

        paramMap.put("accessLogPath", accessLogPath);
        paramMap.put("enableAccessLog", enableAccessLog);

        if (StringUtils.equalsIgnoreCase(enableAccessLog, Boolean.FALSE.toString())) {
            return null;
        }

        RequestLogHandler requestLogHandler = new RequestLogHandler();
        String absolutePath = accessLogPath + "/" + "access.log.yyyy_MM_dd";
        NCSARequestLog requestLog = new NCSARequestLog(absolutePath);
        requestLog.setFilenameDateFormat("yyyy-MM-dd"); // 日志文件的格式
        requestLog.setRetainDays(30); // 日志文件保留的天数
        requestLog.setAppend(true);
        requestLog.setExtended(false); // 禁用UA
        requestLog.setLogDateFormat("HH:mm:ss");
        requestLog.setLogLatency(true);// 请求的处理时间
        requestLog.setLogTimeZone("Asia/Shanghai");// 设置时区,默认为gmt
        requestLogHandler.setRequestLog(requestLog);
        return requestLogHandler;
    }

    private static void showServerConfiguration() {
        StringBuilder builder = new StringBuilder(SystemUtils.LINE_SEPARATOR);
        for (Entry<String, String> entry : paramMap.entrySet()) {
            builder.append(entry.getKey()).append(SPACE).append(entry.getValue());
            builder.append(SystemUtils.LINE_SEPARATOR);
        }

        logger.info(paramMap);// save to logfile
        System.out.println(builder.toString());// show on console
    }
}
