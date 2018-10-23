package cn.moegezi.v2ray.node;

import cn.moegezi.v2ray.node.process.V2rayManager;

public class V2rayNodeApp {

    public static void main(String[] args) throws Exception {
        System.out.println("" +
                "  __  __ _           _           _   _      _\n" +
                " |  \\/  (_)___  __ _| | ____ _  | \\ | | ___| |_\n" +
                " | |\\/| | / __|/ _` | |/ / _` | |  \\| |/ _ \\ __|\n" +
                " | |  | | \\__ \\ (_| |   < (_| | | |\\  |  __/ |_\n" +
                " |_|  |_|_|___/\\__,_|_|\\_\\__,_| |_| \\_|\\___|\\__|\n"
        );
        V2rayManager.getInstance().start();
    }

}
