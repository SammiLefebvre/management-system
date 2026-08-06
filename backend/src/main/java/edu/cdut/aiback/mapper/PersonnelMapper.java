package edu.cdut.aiback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.cdut.aiback.entity.Personnel;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface PersonnelMapper extends BaseMapper<Personnel> {

    @Select("SELECT p.id AS personnelId, p.name, p.role, " +
            "COALESCE(SUM(CASE WHEN w.claimer_id = p.id AND w.status NOT IN ('confirmed','closed') THEN 1 ELSE 0 END),0) AS pendingCount, " +
            "COALESCE(SUM(CASE WHEN w.claimer_id = p.id AND w.status IN ('confirmed','closed') AND w.complete_time >= #{weekStart} THEN 1 ELSE 0 END),0) AS completedThisWeek, " +
            "AVG(CASE WHEN w.claimer_id = p.id AND w.response_duration IS NOT NULL THEN w.response_duration END) AS avgResponseMinutes " +
            "FROM personnel p LEFT JOIN work_order w ON w.claimer_id = p.id " +
            "WHERE p.project_group = #{projectGroup} " +
            "GROUP BY p.id, p.name, p.role")
    List<Map<String, Object>> selectWorkload(@Param("projectGroup") String projectGroup, @Param("weekStart") LocalDateTime weekStart);

    @Select("SELECT p.id, p.account, p.name, p.phone, p.role, p.project_group, p.status, p.latitude, p.longitude, " +
            "  (SELECT COUNT(*) FROM work_order w WHERE w.claimer_id = p.id AND w.status IN ('claimed','in_progress','completing','pending_confirm')) AS pending_count, " +
            "  (SELECT AVG(w.response_duration) FROM work_order w WHERE w.claimer_id = p.id AND w.response_duration IS NOT NULL) AS avg_response, " +
            "  (SELECT COUNT(*) FROM work_order w WHERE w.claimer_id = p.id AND w.status = 'confirmed' AND w.confirm_time >= #{weekStart}) AS completed_week " +
            "FROM personnel p " +
            "WHERE p.role = '外场' AND p.status = 1 AND p.project_group = #{projectGroup} AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "account", column = "account"),
            @Result(property = "name", column = "name"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "role", column = "role"),
            @Result(property = "projectGroup", column = "project_group"),
            @Result(property = "status", column = "status"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "longitude", column = "longitude"),
            @Result(property = "pendingCount", column = "pending_count"),
            @Result(property = "avgResponse", column = "avg_response"),
            @Result(property = "completedWeek", column = "completed_week")
    })
    List<Personnel> selectCandidates(@Param("projectGroup") String projectGroup, @Param("weekStart") LocalDateTime weekStart);
}
