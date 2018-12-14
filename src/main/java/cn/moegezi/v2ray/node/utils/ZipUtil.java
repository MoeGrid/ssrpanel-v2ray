package cn.moegezi.v2ray.node.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

    public static void unzip(File path, File target) throws IOException {
        ZipInputStream zi = new ZipInputStream(new FileInputStream(path));
        ZipEntry ze;
        FileOutputStream fo;
        byte[] buff = new byte[1024];
        int len;
        while ((ze = zi.getNextEntry()) != null) {
            File f = new File(target, ze.getName());
            if (!f.getParentFile().exists())
                f.getParentFile().mkdirs();
            if (ze.isDirectory()) {
                f.mkdir();
            } else {
                fo = new FileOutputStream(f);
                while ((len = zi.read(buff)) > 0) fo.write(buff, 0, len);
                fo.close();
            }
            zi.closeEntry();
        }
        zi.close();
    }

}
