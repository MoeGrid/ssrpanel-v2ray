package cn.moegezi.v2ray.node;

import cn.moegezi.v2ray.node.process.V2rayManager;

import java.io.InputStream;

public class V2rayNodeApp implements Runnable {

    public void run() {
        try {
            InputStream stream = V2rayNodeApp.class.getResourceAsStream("/banner.txt");
            byte[] buffer = new byte[1024];
            int length;
            while ((length = stream.read(buffer)) != -1) {
                System.out.println(new String(buffer, 0, length));
            }
            V2rayManager.getInstance().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(new V2rayNodeApp());
        t.run();
        t.join();
    }

}
