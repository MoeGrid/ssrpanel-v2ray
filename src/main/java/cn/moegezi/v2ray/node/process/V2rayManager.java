package cn.moegezi.v2ray.node.process;

import cn.moegezi.v2ray.node.utils.ConfigUtil;
import org.apache.commons.exec.*;

import java.io.*;

public class V2rayManager {

    private final V2rayDestroyer v2rayDestroyer;
    private final String path = ConfigUtil.getString("v2ray.path");
    private final String exec = ConfigUtil.getString("v2ray.exec");

    private static V2rayManager instance;

    public V2rayManager() {
        this.v2rayDestroyer = V2rayDestroyer.getInstance();
    }

    public void start() throws Exception {
        CommandLine cmdLine = new CommandLine(new File(path, exec));
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(path));
        executor.setProcessDestroyer(v2rayDestroyer);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        executor.execute(cmdLine, resultHandler);
        resultHandler.waitFor();
    }

    public void stop() {
        Process process = v2rayDestroyer.getProcess();
        if (process != null) {
            process.destroyForcibly();
        }
    }

    public static V2rayManager getInstance() {
        if (instance == null) {
            instance = new V2rayManager();
        }
        return instance;
    }

}