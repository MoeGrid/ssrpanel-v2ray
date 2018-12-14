package cn.moegezi.v2ray.node.model;

import java.util.List;

public class V2rayConfig {

    private List<InboundModel> inbounds;

    public List<InboundModel> getInbounds() {
        return inbounds;
    }

    public void setInbounds(List<InboundModel> inbounds) {
        this.inbounds = inbounds;
    }
}
