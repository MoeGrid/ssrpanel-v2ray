package cn.moegezi.v2ray.node.model;

public class NodeInfo {

    private Integer nodeId; // 节点ID
    private Integer uptime; // 运行时间
    private Integer load; // 负载信息
    private Integer logTime; // 记录时间

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getUptime() {
        return uptime;
    }

    public void setUptime(Integer uptime) {
        this.uptime = uptime;
    }

    public Integer getLoad() {
        return load;
    }

    public void setLoad(Integer load) {
        this.load = load;
    }

    public Integer getLogTime() {
        return logTime;
    }

    public void setLogTime(Integer logTime) {
        this.logTime = logTime;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "nodeId=" + nodeId +
                ", uptime=" + uptime +
                ", load=" + load +
                ", logTime=" + logTime +
                '}';
    }
}
