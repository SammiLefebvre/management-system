package edu.cdut.aiback.util;

import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

/**
 * 工单编号生成器 —— 使用数据库行锁保障并发安全
 * 格式: YYYYMMDD/工单类型-0001
 */
@Component
public class WorkOrderCodeGenerator {

    private final DataSource dataSource;

    public WorkOrderCodeGenerator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional
    public String generate(String workOrderType, String projectGroup) throws Exception {
        String datePrefix = DateUtil.format(new Date(), "yyyyMMdd");
        String dateKey = datePrefix + "/" + workOrderType;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 使用 SELECT FOR UPDATE 行锁获取当前序号
                String selectSql = "SELECT current_seq FROM work_order_seq WHERE date_key = ? FOR UPDATE";
                int seq;
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setString(1, dateKey);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            seq = rs.getInt(1) + 1;
                        } else {
                            seq = 1;
                        }
                    }
                }

                // 更新或插入序号
                String upsertSql = "INSERT INTO work_order_seq (date_key, current_seq) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE current_seq = ?";
                try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
                    ps.setString(1, dateKey);
                    ps.setInt(2, seq);
                    ps.setInt(3, seq);
                    ps.executeUpdate();
                }

                conn.commit();
                return String.format("%s-%04d", dateKey, seq);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
