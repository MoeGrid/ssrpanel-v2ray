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
    
}