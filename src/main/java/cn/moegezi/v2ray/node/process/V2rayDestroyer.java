package cn.moegezi.v2ray.node.process;

import cn.moegezi.v2ray.node.utils.PublicUtil;
import org.apache.commons.exec.ProcessDestroyer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class V2rayDestroyer implements ProcessDestroyer {

    private final Logger logger = LoggerFactory.getLogger(V2rayDestroyer.class);

    private static V2rayDestroyer instance;

    private Process process;

    @Override
    public boolean add(Process p) {
        if (process == null) {
            process = p;
            logger.info("V2ray已启动");
            // 设置启动时间
            PublicUtil.setV2rayStartTime();
            // 连接GRPC
            V2rayGrpc.getInstance().start();
            // 启动流量更新线程
            V2rayTimingThread.getInstance().start();
        } else {
            p.destroyForcibly();
        }
        return true;
    }

    @Override
    public boolean remove(Process p) {
        if (p.equals(process)) {
            process = null;
            // 断开GRPC
            V2rayGrpc.getInstance().stop();
            // 停止流量更新线程
            V2rayTimingThread.getInstance().stop();
            logger.info("V2ray已停止");
        }
        return true;
    }

    @Override
    public int size() {
        return process == null ? 0 : 1;
    }

    public Process getProcess() {
        return process;
    }

    public static V2rayDestroyer getInstance() {
        if (instance == null) {
            instance = new V2rayDestroyer();
        }
        return instance;
    }

}
