package cn.moegezi.v2ray.node.process;

import cn.moegezi.v2ray.node.utils.PublicUtil;
import org.apache.commons.exec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * V2Ray进程管理
 */
public class V2rayManager {

    private final Logger logger = LoggerFactory.getLogger(V2rayManager.class);
    private static V2rayManager instance;
    private ExecuteWatchdog watchdog;

    /**
     * 启动V2Ray进程
     */
    public void start() {
        try {
            String version = V2rayUpdate.getInstance().getVersion();
            if (version != null) {
                DefaultExecutor executor = new DefaultExecutor();
                watchdog = new ExecuteWatchdog(Long.MAX_VALUE);
                executor.setWatchdog(watchdog);
                CommandLine cmdLine = new CommandLine(version + "/v2ray");
                DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
                executor.execute(cmdLine, resultHandler);
                PublicUtil.setV2rayStartTime();
            }
        } catch (Exception e) {
            logger.error("V2Ray启动失败: " + e);
        }
    }

    /**
     * 干掉V2Ray进程
     */
    public void stop() {
        if (status()) {
            watchdog.destroyProcess();
        }
    }

    /**
     * 检查进程有没有挂掉
     */
    public void check() {
        if (!status()) {
            start();
            V2rayGrpc.getInstance().restart();
        }
    }

    /**
     * 获取启动状态
     */
    public boolean status() {
        return watchdog != null && watchdog.isWatching();
    }

    public static V2rayManager getInstance() {
        if (instance == null) {
            instance = new V2rayManager();
        }
        return instance;
    }

}