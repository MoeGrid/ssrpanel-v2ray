package cn.moegezi.v2ray.node.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;

public class DbUtil {

    private static QueryRunner queryRunner;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(ConfigUtil.getString("datasource.url"));
        config.setUsername(ConfigUtil.getString("datasource.username"));
        config.setPassword(ConfigUtil.getString("datasource.password"));
        config.setMaximumPoolSize(ConfigUtil.getInteger("datasource.hikari.maximum-pool-size"));
        config.setMinimumIdle(ConfigUtil.getInteger("datasource.hikari.minimum-idle"));
        HikariDataSource dataSource = new HikariDataSource(config);
        queryRunner = new QueryRunner(dataSource);
    }

    public static QueryRunner getQueryRunner() {
        return queryRunner;
    }

    /*
    public static <T> T query(String sql, ResultSetHandler<T> resultSetHandler, Object... params) {
        T result = null;
        try {
            result = queryRunner.query(sql, resultSetHandler, params);
        } catch (Exception e) {
            log.error("", e);
        }
        return result;
    }

    public static int update(String sql, Object... params) {
        int result = 0;
        try {
            result = queryRunner.update(sql, params);
        } catch (Exception e) {
            log.error("", e);
        }
        return result;
    }

    public static int insert(String sql, Object... params) {
        int result = 0;
        try {
            result = queryRunner.execute(sql, params);
        } catch (Exception e) {
            log.error("", e);
        }
        return result;
    }

    public static Map<String, Object> findById(String table, Long id) {
        String sql = "select * from " + table + " where id = ?";
        return query(sql, new MapHandler(), id);
    }

    public static <T> T findById(String table, Long id, BeanHandler<T> beanHandler) {
        String sql = "select * from " + table + " where id = ?";
        return query(sql, beanHandler, id);
    }

    public static List<Map<String, Object>> findByCondition(String table, String condition) {
        String sql = "select * from " + table + " where " + condition;
        return query(sql, new MapListHandler());
    }

    public static <T> List<T> findByCondition(String table, String condition, BeanListHandler<T> beanListHandler) {
        String sql = "select * from " + table + " where " + condition;
        return query(sql, beanListHandler);
    }

    public static List<Map<String, Object>> findByCondition(String table, String condition, String sort) {
        String sql = "select * from " + table + " where " + condition + "order by " + sort;
        return query(sql, new MapListHandler());
    }

    public static List<Map<String, Object>> findByCondition(String table, String condition, String sort, String limit) {
        String sql = "select * from " + table + " where " + condition + "order by " + sort + limit;
        return query(sql, new MapListHandler());
    }

    public static void close() {
        dataSource.close();
    }
    */
}