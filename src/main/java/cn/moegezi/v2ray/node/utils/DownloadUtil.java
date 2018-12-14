package cn.moegezi.v2ray.node.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadUtil {

    private final String url;
    private final File destFilename;

    public DownloadUtil(String url, File destFilename) {
        this.url = url;
        this.destFilename = destFilename;
    }

    public void download() throws IOException {
        FileOutputStream fos = new FileOutputStream(destFilename);
        URLConnection connection = new URL(url).openConnection();
        long fileSize = connection.getContentLengthLong();
        InputStream inputStream = connection.getInputStream();
        byte[] buffer = new byte[10 * 1024 * 1024];
        int numberOfBytesRead;
        long totalNumberOfBytesRead = 0;
        ConsoleProgressBar bar = new ConsoleProgressBar();
        while ((numberOfBytesRead = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, numberOfBytesRead);
            totalNumberOfBytesRead += numberOfBytesRead;
            bar.show(totalNumberOfBytesRead * 100 / fileSize);
        }
        fos.close();
        inputStream.close();
    }
}
