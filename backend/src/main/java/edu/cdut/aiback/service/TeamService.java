package edu.cdut.aiback.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.cdut.aiback.common.BizException;
import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.dto.TeamDTO;
import edu.cdut.aiback.entity.*;
import edu.cdut.aiback.mapper.TeamMapper;
import edu.cdut.aiback.mapper.TeamMemberMapper;
import edu.cdut.aiback.mapper.TeamVehicleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Service
public class TeamService extends ServiceImpl<TeamMapper, Team> {

    private final TeamMemberMapper teamMemberMapper;
    private final TeamVehicleMapper teamVehicleMapper;

    public TeamService(TeamMemberMapper teamMemberMapper, TeamVehicleMapper teamVehicleMapper) {
        this.teamMemberMapper = teamMemberMapper;
        this.teamVehicleMapper = teamVehicleMapper;
    }

    @Transactional
    public Team create(TeamDTO dto) {
        Team team = new Team();
        team.setTeamName(dto.getTeamName());
        team.setProjectGroup(UserContext.getProjectGroup());
        save(team);

        saveMembers(team.getId(), dto);
        return team;
    }

    @Transactional
    public void updateTeam(TeamDTO dto) {
        Team team = getById(dto.getId());
        if (team == null) {
            throw new BizException("班组不存在");
        }
        team.setTeamName(dto.getTeamName());
        updateById(team);

        // 清除旧数据，重新写入
        teamMemberMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, dto.getId()));
        teamVehicleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeamVehicle>()
                .eq(TeamVehicle::getTeamId, dto.getId()));

        saveMembers(dto.getId(), dto);
    }

    /**
     * 按时间段查询班组排班
     */
    public List<Team> listByDateRange(LocalDate startDate, LocalDate endDate) {
        return lambdaQuery()
                .eq(Team::getProjectGroup, UserContext.getProjectGroup())
                .orderByDesc(Team::getUpdatedAt)
                .list();
    }

    /**
     * 获取班组成员（含姓名）
     */
    public List<TeamMember> getMembers(Long teamId) {
        return teamMemberMapper.selectMembersWithName(teamId);
    }

    /**
     * 获取班组车辆
     */
    public List<TeamVehicle> getVehicles(Long teamId) {
        return teamVehicleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeamVehicle>()
                        .eq(TeamVehicle::getTeamId, teamId));
    }

    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        // 同时删除成员和车辆
        teamMemberMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, id));
        teamVehicleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeamVehicle>()
                .eq(TeamVehicle::getTeamId, id));
        return super.removeById(id);
    }

    private void saveMembers(Long teamId, TeamDTO dto) {
        LocalDate date = dto.getDate() != null ? dto.getDate() : LocalDate.now();
        if (dto.getMemberIds() != null) {
            for (Long personnelId : dto.getMemberIds()) {
                TeamMember member = new TeamMember();
                member.setTeamId(teamId);
                member.setPersonnelId(personnelId);
                member.setIsDriver(personnelId.equals(dto.getDriverId()) ? 1 : 0);
                member.setDate(date);
                teamMemberMapper.insert(member);
            }
        }
        if (dto.getVehicles() != null) {
            for (String vehicleName : dto.getVehicles()) {
                TeamVehicle vehicle = new TeamVehicle();
                vehicle.setTeamId(teamId);
                vehicle.setVehicleName(vehicleName);
                vehicle.setDate(date);
                teamVehicleMapper.insert(vehicle);
            }
        }
    }
}
