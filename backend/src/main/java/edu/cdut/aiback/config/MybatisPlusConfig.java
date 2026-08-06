package edu.cdut.aiback.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import edu.cdut.aiback.common.UserContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：分页 + 多租户数据隔离
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 包含 project_group 字段的表名，租户拦截器仅对这些表追加过滤
     */
    private static final java.util.Set<String> TENANT_TABLES = java.util.Set.of(
            "device", "personnel", "work_order", "team"
    );

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        // 多租户数据隔离拦截器
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new MyTenantLineHandler()));

        return interceptor;
    }

    /**
     * 自定义租户处理器
     */
    static class MyTenantLineHandler implements TenantLineHandler {

        @Override
        public Expression getTenantId() {
            String projectGroup = UserContext.getProjectGroup();
            if (projectGroup == null) {
                // 未登录或超级管理员 —— 不过滤
                return new StringValue("__NO_TENANT__");
            }
            return new StringValue(projectGroup);
        }

        @Override
        public String getTenantIdColumn() {
            return "project_group";
        }

        @Override
        public boolean ignoreTable(String tableName) {
            // 未登录时（如登录接口查询 personnel）不追加租户过滤
            if (UserContext.getProjectGroup() == null) {
                return true;
            }
            // 没有 project_group 字段的表不追加过滤
            return !TENANT_TABLES.contains(tableName);
        }

        @Override
        public boolean ignoreInsert(java.util.List<net.sf.jsqlparser.schema.Column> columns, String tenantIdColumn) {
            // 如果 INSERT 语句已包含 project_group 值，则不覆盖
            return columns != null && columns.stream()
                    .anyMatch(col -> tenantIdColumn.equalsIgnoreCase(col.getColumnName()));
        }
    }
}
