## 项目描述
SSRPanel的V2ray节点端

SSRpanel面板: 
[https://github.com/ssrpanel/SSRPanel](https://github.com/ssrpanel/SSRPanel)

## 构建

先装JDK8
```bash
# ubuntu
sudo apt install openjdk-8-jdk
# centos
yum install java-1.8.0-openjdk java-1.8.0-openjdk-devel
```

构建
```bash
git clone https://github.com/aiyahacke/ssrpanel-v2ray.git
cd ssrpanel-v2ray
git submodule update --init --recursive
chmod +x ./mvnw
./mvnw package
```

## 配置V2ray

下载V2ray
[https://github.com/v2ray/v2ray-core/releases](https://github.com/v2ray/v2ray-core/releases)

按照 config.json 为模板修改，可以添加一些自定义的配置

## 配置SSRPanel后台
```
添加节点
基础信息按实际情况填写
扩展信息的服务类型选择V2ray

基础选项:
  额外ID (alterId)
  端口 (VMess协议的端口)
  
高级选项(不懂就不要动下面几个选项): 
  传输协议
  伪装类型
  伪装域名
  WS/H2路径
  TLS
  (相见官方文档 https://v2ray.com/chapter_02/05_transport.html)
```

## 配置节点端
```
将 config.properties 和 target 目录中的 jar 文件和 lib 目录放到一起
修改 config.properties

几个重点配置项
  v2ray.grpc.port (tag为api的传入连接的端口)
  v2ray.tag (VMess协议的tag)
  v2ray.alter-id (与面板额外ID一致)
  node.id (面板添加节点后得到的节点ID)
  node.traffic-rate (与面板流量比例一致)

数据库配置(远程连接SSRPanel的数据库)
  datasource.url (数据库的连接URL, 格式为 jdbc:mysql://地址:端口/数据库名称?serverTimezone=GMT%2B8)
  datasource.username (用户名)
  datasource.password (密码)
```

执行 `java -jar xxxxx.jar`即可
