package cn.moegezi.v2ray.node.process;

import cn.moegezi.v2ray.node.model.UserModel;
import cn.moegezi.v2ray.node.model.UserTrafficLog;
import cn.moegezi.v2ray.node.utils.ConfigUtil;
import com.google.protobuf.ByteString;
import com.v2ray.core.InboundHandlerConfig;
import com.v2ray.core.app.proxyman.ReceiverConfig;
import com.v2ray.core.app.proxyman.command.*;
import com.v2ray.core.app.stats.command.GetStatsRequest;
import com.v2ray.core.app.stats.command.GetStatsResponse;
import com.v2ray.core.app.stats.command.StatsServiceGrpc;
import com.v2ray.core.common.net.IPOrDomain;
import com.v2ray.core.common.net.Network;
import com.v2ray.core.common.net.PortRange;
import com.v2ray.core.common.protocol.SecurityConfig;
import com.v2ray.core.common.protocol.SecurityType;
import com.v2ray.core.common.protocol.User;
import com.v2ray.core.common.serial.TypedMessage;
import com.v2ray.core.proxy.shadowsocks.Account;
import com.v2ray.core.proxy.shadowsocks.CipherType;
import com.v2ray.core.proxy.shadowsocks.ServerConfig;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class V2rayGrpc {

    private final Logger logger = LoggerFactory.getLogger(V2rayGrpc.class);
    private final V2rayDao v2rayDao;
    private final String v2rayTag = ConfigUtil.getString("v2ray.tag");
    private final String security = ConfigUtil.getString("v2ray.security");
    private final Integer alterId = ConfigUtil.getInteger("v2ray.alter-id");
    private final Integer level = ConfigUtil.getInteger("v2ray.level");
    private final String address = ConfigUtil.getString("v2ray.grpc.address");
    private final Integer port = ConfigUtil.getInteger("v2ray.grpc.port");

    private static final String UplinkFormat = "user>>>%s>>>traffic>>>uplink";
    private static final String DownlinkFormat = "user>>>%s>>>traffic>>>downlink";

    private static V2rayGrpc instance;

    private ManagedChannel channel;

    private List<UserModel> users = new ArrayList<>();

    private V2rayGrpc() {
        this.v2rayDao = V2rayDao.getInstance();
    }

    public void start() {
        if (channel != null && !channel.isShutdown()) stop();
        channel = ManagedChannelBuilder.forAddress(address, port).usePlaintext().build();
    }

    public void stop() {
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    public void update() {
        List<UserTrafficLog> list = new ArrayList<>();
        for (UserModel i : users) {
            long up = getTraffic(i.getEmail(), UplinkFormat);
            long down = getTraffic(i.getEmail(), DownlinkFormat);
            if (up != 0 || down != 0) {
                UserTrafficLog t = new UserTrafficLog();
                t.setUserId(i.getId());
                t.setU(up);
                t.setD(down);
                list.add(t);
            }
        }
        if (list.size() > 0) {
            v2rayDao.trafficLog(list);
            v2rayDao.updateUserTraffic(list);
        }
        v2rayDao.nodeOnlineLog(list);
        v2rayDao.updateNodeInfo();
        syncUser();
    }

    private void syncUser() {
        List<UserModel> dbUsers = v2rayDao.getAllUser();
        List<UserModel> add = new ArrayList<>();
        List<UserModel> remove = new ArrayList<>();
        // 删除过期用户
        for (UserModel i : users) {
            if (!dbUsers.contains(i)) {
                removeUser(i.getEmail());
                remove.add(i);
            }
        }
        users.removeAll(remove);
        // 添加新用户
        for (UserModel i : dbUsers) {
            if (!users.contains(i)) {
                addUser(i);
                add.add(i);
            }
        }
        users.addAll(add);
        if (add.size() > 0 || remove.size() > 0) {
            logger.info("更新用户: ADD " + add.size() + " REMOVE " + remove.size());
        }
    }

    // 添加用户
    private void addUser(UserModel userModel) {
        HandlerServiceGrpc.HandlerServiceBlockingStub handlerService = HandlerServiceGrpc.newBlockingStub(channel);
        AlterInboundRequest req = AlterInboundRequest
                .newBuilder()
                .setTag(v2rayTag)
                .setOperation(TypedMessage
                        .newBuilder()
                        .setType(AddUserOperation.getDescriptor().getFullName())
                        .setValue(AddUserOperation
                                .newBuilder()
                                .setUser(User
                                        .newBuilder()
                                        .setLevel(level)
                                        .setEmail(userModel.getEmail())
                                        .setAccount(TypedMessage
                                                .newBuilder()
                                                .setType(com.v2ray.core.proxy.vmess.Account.getDescriptor().getFullName())
                                                .setValue(com.v2ray.core.proxy.vmess.Account
                                                        .newBuilder()
                                                        .setId(userModel.getVmessId())
                                                        .setAlterId(alterId)
                                                        .setSecuritySettings(SecurityConfig
                                                                .newBuilder()
                                                                .setType(str2SecurityType(security))
                                                                .build())
                                                        .build()
                                                        .toByteString())
                                                .build())
                                        .build())
                                .build()
                                .toByteString())
                        .build())
                .build();
        try {
            handlerService.alterInbound(req);
        } catch (StatusRuntimeException e) {
            logger.error("添加用户失败" + e);
        }
    }

    // 删除用户
    private void removeUser(String email) {
        HandlerServiceGrpc.HandlerServiceBlockingStub handlerService = HandlerServiceGrpc.newBlockingStub(channel);
        AlterInboundRequest req = AlterInboundRequest
                .newBuilder()
                .setTag(v2rayTag)
                .setOperation(TypedMessage
                        .newBuilder()
                        .setType(RemoveUserOperation.getDescriptor().getFullName())
                        .setValue(RemoveUserOperation
                                .newBuilder()
                                .setEmail(email)
                                .build()
                                .toByteString())
                        .build())
                .build();
        try {
            handlerService.alterInbound(req);
        } catch (StatusRuntimeException e) {
            logger.error("删除用户失败", e);
        }
    }

    public void addSsUser() {
        HandlerServiceGrpc.HandlerServiceBlockingStub handlerService = HandlerServiceGrpc.newBlockingStub(channel);
        AddInboundRequest req = AddInboundRequest
                .newBuilder()
                .setInbound(InboundHandlerConfig
                        .newBuilder()
                        .setTag("SSTag")
                        .setReceiverSettings(TypedMessage
                                .newBuilder()
                                .setType(ReceiverConfig.getDescriptor().getFullName())
                                .setValue(ReceiverConfig
                                        .newBuilder()
                                        .setPortRange(PortRange
                                                .newBuilder()
                                                .setFrom(10088)
                                                .setTo(10088)
                                                .build())
                                        .setListen(IPOrDomain
                                                .newBuilder()
                                                .setIp(ByteString.copyFrom(new byte[]{0, 0, 0, 0}))
                                                .build())
                                        .build()
                                        .toByteString())
                                .build())
                        .setProxySettings(TypedMessage
                                .newBuilder()
                                .setType(ServerConfig.getDescriptor().getFullName())
                                .setValue(ServerConfig
                                        .newBuilder()
                                        .setUser(User
                                                .newBuilder()
                                                .setAccount(TypedMessage
                                                        .newBuilder()
                                                        .setType(com.v2ray.core.proxy.shadowsocks.Account.getDescriptor().getFullName())
                                                        .setValue(com.v2ray.core.proxy.shadowsocks.Account
                                                                .newBuilder()
                                                                .setPassword("password")
                                                                .setCipherType(CipherType.CHACHA20)
                                                                .setOta(Account.OneTimeAuth.Auto)
                                                                .build()
                                                                .toByteString())
                                                        .build())
                                                .setLevel(1)
                                                .build())
                                        .addNetwork(Network.TCP)
                                        .build()
                                        .toByteString())
                                .build())
                        .build())
                .build();
        try {
            handlerService.addInbound(req);
        } catch (StatusRuntimeException e) {
            logger.error("添加SS用户失败", e);
        }
    }

    // 获得用户流量
    private long getTraffic(String email, String fmt) {
        StatsServiceGrpc.StatsServiceBlockingStub statsService = StatsServiceGrpc.newBlockingStub(channel);
        email = String.format(fmt, email);
        GetStatsRequest req = GetStatsRequest
                .newBuilder()
                .setReset(true)
                .setName(email)
                .build();
        try {
            GetStatsResponse res = statsService.getStats(req);
            long t = res.getStat().getValue();
            logger.info("获取用户流量: USER " + email + " TRAFFIC " + t);
            return t;
        } catch (StatusRuntimeException e) {
            if (!e.getMessage().contains(email + " not found"))
                logger.error("获取用户流量失败", e);
            return 0;
        }
    }

    private SecurityType str2SecurityType(String str) {
        switch (str) {
            case "aes-128-gcm":
                return SecurityType.AES128_GCM;
            case "chacha20-poly1305":
                return SecurityType.CHACHA20_POLY1305;
            case "none":
                return SecurityType.NONE;
            case "auto":
                return SecurityType.AUTO;
            default:
                return SecurityType.AUTO;
        }
    }

    public static V2rayGrpc getInstance() {
        if (instance == null) {
            instance = new V2rayGrpc();
        }
        return instance;
    }

}
