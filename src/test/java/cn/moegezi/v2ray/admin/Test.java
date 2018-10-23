package cn.moegezi.v2ray.admin;

import java.io.File;

public class Test {

    public static void main(String[] args) {
        String path = System.getProperty("user.dir") + "/src/main/proto/v2ray.com/";
        File f = new File(path);
        if (f.isDirectory()) {
            deleteFile(f);
        }
    }

    private static void deleteFile(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    String fn = f.getName();
                    String suffix = fn.substring(fn.lastIndexOf(".") + 1);
                    if (!suffix.equals("proto")) {
                        if (!f.delete()) {
                            System.out.println("删除文件失败：" + f.getAbsolutePath());
                        }
                    }
                } else if (f.isDirectory()) {
                    deleteFile(f);
                    File[] files2 = f.listFiles();
                    if (files2 == null || files2.length <= 0) {
                        if (!f.delete()) {
                            System.out.println("删除目录失败：" + f.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }

}
