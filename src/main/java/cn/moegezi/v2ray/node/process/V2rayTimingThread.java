package cn.moegezi.v2ray.node.process;

import cn.moegezi.v2ray.node.utils.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class V2rayTimingThread {

    private static V2rayTimingThread instance;

    private final Logger logger = LoggerFactory.getLogger(V2rayTimingThread.class);
    private final long checkRate = ConfigUtil.getLong("node.check-rate");

    private ScheduledExecutorService scheduExec;
    private int lastUpdateDay = 0;

    public V2rayTimingThread() {
        this.scheduExec = Executors.newScheduledThreadPool(2);
    }

    public void start() {
        scheduExec.scheduleAtFixedRate(() -> {
            try {
                Calendar cal = Calendar.getInstance();
                int tmp = cal.get(Calendar.DATE);
                if (tmp != lastUpdateDay) {
                    V2rayUpdate.getInstance().checkUpdate();
                    lastUpdateDay = tmp;
                }
                V2rayManager.getInstance().check();
                V2rayGrpc.getInstance().update();
            } catch (Exception e) {
                logger.error("定时任务异常", e);
            }
        }, 0L, checkRate, TimeUnit.SECONDS);
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