package edu.cdut.aiback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.cdut.aiback.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {

    @Select("SELECT tm.*, p.name as personnel_name FROM team_member tm " +
            "LEFT JOIN personnel p ON tm.personnel_id = p.id " +
            "WHERE tm.team_id = #{teamId} ORDER BY tm.created_at")
    List<TeamMember> selectMembersWithName(@Param("teamId") Long teamId);
}
