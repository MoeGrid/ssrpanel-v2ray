package cn.moegezi.v2ray.node.process;

import cn.moegezi.v2ray.node.utils.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class V2rayUpdate {

    private static final String GITHUB_API = "https://api.github.com/repos/v2ray/v2ray-core/releases/latest";

    private static V2rayUpdate instance;

    private final Logger logger = LoggerFactory.getLogger(V2rayUpdate.class);
    private final String system = ConfigUtil.getString("v2ray.system");
    private final String arch = ConfigUtil.getString("v2ray.arch");

    private String version;

    public boolean checkUpdate() {
        logger.info("开始检测V2Ray更新");
        try {
            String res = HttpUtil.httpsRequest(GITHUB_API, "GET", null);
            if (res != null) {
                JSONObject json = JSON.parseObject(res);
                String tagName = json.getString("tag_name");
                File dir = new File(tagName);
                if (!dir.exists() || !dir.isDirectory()) {
                    logger.info("检测到新版本V2Ray: " + tagName);
                    // 需要更新 获得下载地址
                    String filename = String.format("v2ray-%s-%s.zip", system, arch);
                    String downloadUrl = null;
                    JSONArray arr = json.getJSONArray("assets");
                    for (int i = 0; i < arr.size(); i++) {
                        JSONObject item = arr.getJSONObject(i);
                        if (filename.equals(item.getString("name"))) {
                            downloadUrl = item.getString("browser_download_url");
                        }
                    }
                    if (downloadUrl != null) {
                        File tmp = new File("tmp");
                        // 重建临时目录
                        if (tmp.exists() && tmp.isDirectory()) {
                            deleteDir(tmp);
                        }
                        tmp.mkdir();
                        // 开始下载
                        logger.info("开始下载: " + downloadUrl);
                        File zip = new File(tmp, filename);
                        DownloadUtil down = new DownloadUtil(downloadUrl, zip);
                        down.download();
                        // 开始解压
                        logger.info("开始解压: " + filename);
                        ZipUtil.unzip(zip, tmp);
                        // 删除压缩文件
                        zip.delete();
                        // 重命名目录
                        tmp.renameTo(dir);
                        // 更改权限
                        if (PublicUtil.isLinux()) {
                            new File(dir, "v2ray").setExecutable(true);
                            new File(dir, "v2ctl").setExecutable(true);
                        }
                    } else {
                        throw new Exception("找不到更新文件");
                    }
                    V2rayManager.getInstance().stop();
                    logger.info("V2Ray已更新至 " + tagName);
                } else {
                    logger.info("V2Ray已是最新版本");
                }
                version = tagName;
                return true;
            } else {
                throw new Exception("获取更新失败");
            }
        } catch (Exception e) {
            logger.info("更新失败: ", e);
            return false;
        }
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }

    public String getVersion() {
        return version;
    }

    public static V2rayUpdate getInstance() {
        if (instance == null) {
            instance = new V2rayUpdate();
        }
        return instance;
    }

}
