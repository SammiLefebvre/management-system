package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.Result;
import edu.cdut.aiback.dto.TeamDTO;
import edu.cdut.aiback.entity.Team;
import edu.cdut.aiback.entity.TeamMember;
import edu.cdut.aiback.entity.TeamVehicle;
import edu.cdut.aiback.service.TeamService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * 按时间段查询班组排班列表
     */
    @GetMapping("/list")
    public Result<List<Team>> list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.ok(teamService.listByDateRange(startDate, endDate));
    }

    /**
     * 班组详情（含成员、车辆）
     */
    @GetMapping("/{id}")
    public Result<java.util.Map<String, Object>> detail(@PathVariable Long id) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("team", teamService.getById(id));
        result.put("members", teamService.getMembers(id));
        result.put("vehicles", teamService.getVehicles(id));
        return Result.ok(result);
    }

    /**
     * 班组成员列表
     */
    @GetMapping("/{id}/members")
    public Result<List<TeamMember>> members(@PathVariable Long id) {
        return Result.ok(teamService.getMembers(id));
    }

    /**
     * 班组车辆列表
     */
    @GetMapping("/{id}/vehicles")
    public Result<List<TeamVehicle>> vehicles(@PathVariable Long id) {
        return Result.ok(teamService.getVehicles(id));
    }

    /**
     * 创建班组
     */
    @PostMapping
    public Result<Team> create(@RequestBody TeamDTO dto) {
        return Result.ok(teamService.create(dto));
    }

    /**
     * 编辑班组
     */
    @PutMapping
    public Result<Void> update(@RequestBody TeamDTO dto) {
        teamService.updateTeam(dto);
        return Result.ok();
    }

    /**
     * 删除班组
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(teamService.removeById(id));
    }
}
