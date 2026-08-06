package edu.cdut.aiback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.cdut.aiback.entity.WorkOrderSeq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 工单编号序列 Mapper —— 使用 FOR UPDATE 行锁保障并发安全
 */
@Mapper
public interface WorkOrderSeqMapper extends BaseMapper<WorkOrderSeq> {

    @Select("SELECT * FROM work_order_seq WHERE date_key = #{dateKey} FOR UPDATE")
    WorkOrderSeq selectByKeyForUpdate(@Param("dateKey") String dateKey);

    @Update("UPDATE work_order_seq SET current_seq = current_seq + 1 WHERE date_key = #{dateKey}")
    int incrementSeq(@Param("dateKey") String dateKey);

    @Select("SELECT IFNULL(MAX(current_seq), 0) FROM work_order_seq WHERE date_key = #{dateKey}")
    int getCurrentSeq(@Param("dateKey") String dateKey);
}
