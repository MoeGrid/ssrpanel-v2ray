package cn.moegezi.v2ray.node;

import cn.moegezi.v2ray.node.process.V2rayGrpc;
import cn.moegezi.v2ray.node.process.V2rayTimingThread;
import cn.moegezi.v2ray.node.process.V2rayUpdate;

public class V2rayNodeApp implements Runnable {

    private V2rayNodeApp() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            V2rayTimingThread.getInstance().stop();
        }));
    }

    public void run() {
        System.out.println("" +
                "  __  __ _           _           _   _      _\n" +
                " |  \\/  (_)___  __ _| | ____ _  | \\ | | ___| |_\n" +
                " | |\\/| | / __|/ _` | |/ / _` | |  \\| |/ _ \\ __|\n" +
                " | |  | | \\__ \\ (_| |   < (_| | | |\\  |  __/ |_\n" +
                " |_|  |_|_|___/\\__,_|_|\\_\\__,_| |_| \\_|\\___|\\__|\n"
        );
        //if (V2rayUpdate.getInstance().checkUpdate()) {
            V2rayTimingThread.getInstance().start();
        //}
    }

    public static void main(String[] args) throws InterruptedException {
        V2rayNodeApp app = new V2rayNodeApp();
        Thread t = new Thread(app);
        t.run();
        t.join();
    }

}
