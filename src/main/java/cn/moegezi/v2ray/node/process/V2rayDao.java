package cn.moegezi.v2ray.node.process;

import cn.moegezi.v2ray.node.model.UserModel;
import cn.moegezi.v2ray.node.model.UserTrafficLog;
import cn.moegezi.v2ray.node.utils.ConfigUtil;
import cn.moegezi.v2ray.node.utils.DbUtil;
import cn.moegezi.v2ray.node.utils.LRUCache;
import cn.moegezi.v2ray.node.utils.PublicUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class V2rayDao {

    private static V2rayDao instance;

    private final Logger logger = LoggerFactory.getLogger(V2rayDao.class);
    private final QueryRunner db;

    private static final String GET_ALL_USER = "SELECT id, vmess_id vmessId FROM user WHERE enable = 1 AND u + d < transfer_enable";
    private static final String TRAFFIC_LOG = "INSERT INTO `user_traffic_log` (`user_id`, `u`, `d`, `node_id`, `rate`, `traffic`, `log_time`) VALUES (?, ?, ?, ?, ?, ?, unix_timestamp())";
    private static final String UPDATE_USER_TRAFFIC = "UPDATE user SET u = CASE id %s END, d = CASE id %s END, t = %d WHERE id IN (%s)";
    private static final String NODE_ONLINE_LOG = "INSERT INTO `ss_node_online_log` (`node_id`, `online_user`, `log_time`) VALUES (?, ?, unix_timestamp())";
    private static final String UPDATE_NODE_INFO = "INSERT INTO `ss_node_info` (`node_id`, `uptime`, `load`, `log_time`) VALUES (?, ? , ?, unix_timestamp())";

    private final Double trafficRate = ConfigUtil.getDouble("node.traffic-rate");
    private final Integer nodeId = ConfigUtil.getInteger("node.id");
    private final LRUCache localCache = new LRUCache(60 * 30 * 1000);

    public V2rayDao() {
        this.db = DbUtil.getQueryRunner();
    }

    // 1. 记录流量日志
    public void trafficLog(List<UserTrafficLog> traffic) {
        long num = 0;
        Object[][] param = new Object[traffic.size()][6];
        for (int i = 0; i < param.length; i++) {
            UserTrafficLog t = traffic.get(i);
            param[i][0] = t.getUserId();
            param[i][1] = t.getU();
            param[i][2] = t.getD();
            param[i][3] = nodeId;
            param[i][4] = trafficRate;
            param[i][5] = PublicUtil.trafficFormat(t.getU() + t.getD());
            num += t.getU() + t.getD();
        }
        try {
            db.batch(TRAFFIC_LOG, param);
            logger.error("记录流量日志: USER_NUM " + traffic.size() + " ALL_TRAFFIC " + PublicUtil.trafficFormat(num));
        } catch (SQLException e) {
            logger.error("记录流量日志异常", e);
        }
    }

    // 2. 记录用户流量信息
    public void updateUserTraffic(List<UserTrafficLog> list) {
        long t = System.currentTimeMillis() / 1000;
        StringBuilder ids = new StringBuilder();
        StringBuilder uSql = new StringBuilder();
        StringBuilder dSql = new StringBuilder();
        for (UserTrafficLog i : list) {
            ids.append(i.getUserId()).append(",");
            uSql.append(String.format(" WHEN %d THEN u + %d ", i.getUserId(), i.getU()));
            dSql.append(String.format(" WHEN %d THEN d + %d ", i.getUserId(), i.getD()));
        }
        ids.deleteCharAt(ids.length() - 1);
        try {
            String sql = String.format(UPDATE_USER_TRAFFIC, uSql, dSql, t, ids);
            db.execute(sql);
            logger.error("更新用户流量信息: USER_NUM " + list.size());
        } catch (SQLException e) {
            logger.error("更新用户流量信息异常", e);
        }
    }

    // 3. 记录节点在线信息
    public void nodeOnlineLog(List<UserTrafficLog> list) {
        localCache.sweep();
        for (UserTrafficLog i : list) {
            localCache.put(i.getUserId());
        }
        try {
            db.execute(NODE_ONLINE_LOG, nodeId, localCache.size());
            logger.info("更新在线用户数: NUMBER " + localCache.size());
        } catch (SQLException e) {
            logger.error("更新节点在线信息异常", e);
        }
    }

    // 4. 记录节点负载信息
    public void updateNodeInfo() {
        String load = "0.00 0.00 0.00";
        if (PublicUtil.isLinux()) {
            load = PublicUtil.exec("cat /proc/loadavg");
            String[] loads = load.split(" ");
            if (loads.length >= 3) {
                load = loads[0] + ' ' + loads[1] + ' ' + loads[2];
            }
        }
        try {
            db.execute(UPDATE_NODE_INFO, nodeId, PublicUtil.getV2rayUpTime() / 1000, load);
            logger.info("更新节点负载信息: LOAD " + load);
        } catch (SQLException e) {
            logger.error("更新节点负载信息异常", e);
        }
    }

    // 4. 更新所有用户
    public List<UserModel> getAllUser() {
        List<UserModel> result = null;
        try {
            result = db.query(GET_ALL_USER, new BeanListHandler<>(UserModel.class));
        } catch (SQLException e) {
            logger.error("更新用户异常", e);
        }
        return result;
    }

    public static V2rayDao getInstance() {
        if (instance == null) {
            instance = new V2rayDao();
        }
        return instance;
    }

}
