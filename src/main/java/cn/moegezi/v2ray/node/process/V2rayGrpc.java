package cn.moegezi.v2ray.node.process;

import cn.moegezi.v2ray.node.model.InboundModel;
import cn.moegezi.v2ray.node.model.UserModel;
import cn.moegezi.v2ray.node.model.UserTrafficLog;
import cn.moegezi.v2ray.node.utils.ConfigUtil;
import cn.moegezi.v2ray.node.utils.V2rayConfigUtil;
import com.v2ray.core.app.proxyman.command.*;
import com.v2ray.core.app.stats.command.GetStatsRequest;
import com.v2ray.core.app.stats.command.GetStatsResponse;
import com.v2ray.core.app.stats.command.StatsServiceGrpc;
import com.v2ray.core.common.protocol.SecurityConfig;
import com.v2ray.core.common.protocol.User;
import com.v2ray.core.common.serial.TypedMessage;
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
    private final String v2rayTag = ConfigUtil.getString("v2ray.tag");
    private final Integer alterId = ConfigUtil.getInteger("v2ray.alter-id");
    private final Integer level = ConfigUtil.getInteger("v2ray.level");

    private static final String UplinkFormat = "user>>>%s>>>traffic>>>uplink";
    private static final String DownlinkFormat = "user>>>%s>>>traffic>>>downlink";

    private static V2rayGrpc instance;

    private ManagedChannel channel;

    private List<UserModel> users = new ArrayList<>();

    public void start() {
        InboundModel inbound = V2rayConfigUtil.getInboundByTag("api");
        if (inbound != null) {
            channel = ManagedChannelBuilder.forAddress("127.0.0.1", inbound.getPort()).usePlaintext().build();
        } else {
            logger.error("没有找到tag为api的传入连接");
        }
    }

    public void stop() {
        if (channel == null) return;
        try {
            users.clear();
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    public void restart() {
        stop();
        start();
    }

    public void update() {
        /* 更新节点和用户情况 */

        if (!V2rayManager.getInstance().status()) return;
        List<UserTrafficLog> list = new ArrayList<>();
        V2rayDao v2rayDao = V2rayDao.getInstance();
        for (UserModel i : users) {
            long up = getTraffic(i, UplinkFormat);
            long down = getTraffic(i, DownlinkFormat);
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

        /* 同步用户 */

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

    // 获得用户流量
    private long getTraffic(UserModel user, String fmt) {
        StatsServiceGrpc.StatsServiceBlockingStub statsService = StatsServiceGrpc.newBlockingStub(channel);
        String q = String.format(fmt, user.getEmail());
        GetStatsRequest req = GetStatsRequest
                .newBuilder()
                .setReset(true)
                .setName(q)
                .build();
        try {
            GetStatsResponse res = statsService.getStats(req);
            long t = res.getStat().getValue();
            logger.info("获取用户流量: USER " + user.getId() + " TRAFFIC " + t);
            return t;
        } catch (StatusRuntimeException e) {
            if (!e.getMessage().contains(q + " not found"))
                logger.error("获取用户流量失败", e);
            return 0;
        }
    }

    public static V2rayGrpc getInstance() {
        if (instance == null) {
            instance = new V2rayGrpc();
        }
        return instance;
    }

    /*
    // 添加SS连接
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
    */

}
