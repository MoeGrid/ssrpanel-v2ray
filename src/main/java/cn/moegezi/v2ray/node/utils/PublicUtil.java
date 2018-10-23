package cn.moegezi.v2ray.node.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PublicUtil {

    private static Boolean isLinux = null;
    private static Long v2rayStartTime = null;

    public static boolean isLinux() {
        if (isLinux == null) {
            isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        }
        return isLinux;
    }

    public static void setV2rayStartTime() {
        v2rayStartTime = System.currentTimeMillis();
    }

    public static long getV2rayUpTime() {
        return System.currentTimeMillis() - v2rayStartTime;
    }

    public static String exec(String command) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(command);
            DefaultExecutor exec = new DefaultExecutor();
            exec.setExitValues(null);
            ExecuteWatchdog watchdog = new ExecuteWatchdog(10000);
            exec.setWatchdog(watchdog);
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
            exec.setStreamHandler(streamHandler);
            exec.execute(commandline);
            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static String trafficFormat(Long traffic) {
        BigDecimal b = new BigDecimal(traffic);
        if (traffic < 1024 * 8)
            return b + "B";
        if (traffic < 1024 * 1024 * 2)
            return b.divide(new BigDecimal(1024), 2, RoundingMode.HALF_UP) + "KB";
        return b.divide(new BigDecimal(1048576), 2, RoundingMode.HALF_UP) + "MB";
    }

}
