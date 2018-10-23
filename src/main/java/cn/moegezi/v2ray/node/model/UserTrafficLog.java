package cn.moegezi.v2ray.node.model;

public class UserTrafficLog {

    private Integer userId; // 用户ID
    private Long u; // 上传流量
    private Long d; // 下载流量

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getU() {
        return u;
    }

    public void setU(Long u) {
        this.u = u;
    }

    public Long getD() {
        return d;
    }

    public void setD(Long d) {
        this.d = d;
    }

    @Override
    public String toString() {
        return "UserTrafficLog{" +
                ", userId=" + userId +
                ", u=" + u +
                ", d=" + d +
                '}';
    }
}
