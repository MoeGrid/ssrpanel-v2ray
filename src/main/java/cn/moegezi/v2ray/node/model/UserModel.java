package cn.moegezi.v2ray.node.model;

import java.util.Objects;

public class UserModel {

    private Integer id;
    private String vmessId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVmessId() {
        return vmessId;
    }

    public void setVmessId(String vmessId) {
        this.vmessId = vmessId;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "id=" + id +
                ", vmessId='" + vmessId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserModel)) return false;
        UserModel userModel = (UserModel) o;
        return Objects.equals(getId(), userModel.getId()) &&
                Objects.equals(getVmessId(), userModel.getVmessId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getVmessId());
    }

    public String getEmail() {
        return getId() + "@v2ray.com";
    }

}
