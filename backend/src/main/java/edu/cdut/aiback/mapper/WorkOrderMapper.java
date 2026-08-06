package edu.cdut.aiback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.cdut.aiback.entity.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 工单 Mapper
 */
@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    /**
     * 获取当天的最大序号（按工单类型和日期分组，用于生成工单编号）
     * 使用 FOR UPDATE 行锁防止并发重复
     */
    @Select("SELECT MAX(CAST(SUBSTRING_INDEX(work_order_code, '-', -1) AS UNSIGNED)) " +
            "FROM work_order " +
            "WHERE work_order_code LIKE CONCAT(#{datePrefix}, '%') " +
            "FOR UPDATE")
    Integer selectMaxSeqByDateAndType(@Param("datePrefix") String datePrefix);

    @Select("SELECT d.area AS area, w.fault_type AS faultType, COUNT(*) AS cnt " +
            "FROM work_order w LEFT JOIN device d ON w.device_id = d.id " +
            "WHERE w.project_group = #{projectGroup} AND w.fault_type IS NOT NULL AND d.area IS NOT NULL " +
            "GROUP BY d.area, w.fault_type")
    List<Map<String, Object>> selectFaultHeatmap(@Param("projectGroup") String projectGroup);
}
