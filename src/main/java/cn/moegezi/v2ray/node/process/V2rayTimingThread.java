package cn.moegezi.v2ray.node.process;

import cn.moegezi.v2ray.node.utils.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class V2rayTimingThread implements Runnable {

    private static V2rayTimingThread instance;

    private final Logger logger = LoggerFactory.getLogger(V2rayTimingThread.class);
    private final V2rayGrpc v2rayGrpc;
    private final long checkRate = ConfigUtil.getLong("node.check-rate");

    private ScheduledExecutorService scheduExec;

    public V2rayTimingThread() {
        this.scheduExec = Executors.newScheduledThreadPool(2);
        this.v2rayGrpc = V2rayGrpc.getInstance();
    }

    @Override
    public void run() {
        try {
            v2rayGrpc.update();
        } catch (Exception e) {
            logger.error("定时任务异常", e);
        }
    }

    public void start() {
        scheduExec.scheduleAtFixedRate(this, 0L, checkRate, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduExec.shutdownNow();
    }

    public static V2rayTimingThread getInstance() {
        if (instance == null) {
            instance = new V2rayTimingThread();
        }
        return instance;
    }

}
