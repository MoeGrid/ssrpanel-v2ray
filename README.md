## 项目描述
SSRPanel的V2ray节点端

SSRpanel面板: 
[https://github.com/ssrpanel/SSRPanel](https://github.com/ssrpanel/SSRPanel)

## 构建

先装JDK8，方法不多说，百度google一大堆
```bash
git clone https://github.com/aiyahacke/ssrpanel-v2ray.git
cd ssrpanel-v2ray
git submodule update --init --recursive
./mvnw package
```

## 配置V2ray

下载:
[https://github.com/v2ray/v2ray-core/releases](https://github.com/v2ray/v2ray-core/releases)

按照 config.json 模板修改

## 配置后台&节点端

在SSRPanel按照配置添加节点

将 config.properties 和 target 目录中的 jar 文件和 lib 目录放到一起

按照注释修改 config.properties 文件与面板设置一致

执行 `java -jar xxxxx.jar`即可
