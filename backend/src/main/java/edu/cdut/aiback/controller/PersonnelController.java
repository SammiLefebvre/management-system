package edu.cdut.aiback.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.cdut.aiback.common.Result;
import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.dto.PersonnelQueryDTO;
import edu.cdut.aiback.entity.Personnel;
import edu.cdut.aiback.service.PersonnelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personnel")
public class PersonnelController {

    private final PersonnelService personnelService;

    public PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }

    /**
     * 分页列表
     */
    @GetMapping("/page")
    public Result<Page<Personnel>> page(PersonnelQueryDTO query) {
        return Result.ok(personnelService.page(query));
    }

    /**
     * 外场人员列表（班组选择成员用）
     */
    @GetMapping("/external")
    public Result<List<Personnel>> listExternal() {
        return Result.ok(personnelService.listExternal(UserContext.getProjectGroup()));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<Personnel> getById(@PathVariable Long id) {
        return Result.ok(personnelService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody Personnel personnel) {
        return Result.ok(personnelService.save(personnel));
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Personnel personnel) {
        return Result.ok(personnelService.updateById(personnel));
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(personnelService.removeById(id));
    }
}
