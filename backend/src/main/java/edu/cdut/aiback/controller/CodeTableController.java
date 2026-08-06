package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.Result;
import edu.cdut.aiback.entity.CodeTable;
import edu.cdut.aiback.service.CodeTableService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/code-table")
public class CodeTableController {

    private final CodeTableService codeTableService;

    public CodeTableController(CodeTableService codeTableService) {
        this.codeTableService = codeTableService;
    }

    /**
     * 按类型获取码表
     */
    @GetMapping("/{codeType}")
    public Result<List<CodeTable>> listByType(@PathVariable String codeType) {
        return Result.ok(codeTableService.listByType(codeType));
    }

    /**
     * 新增码值
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody CodeTable codeTable) {
        return Result.ok(codeTableService.save(codeTable));
    }

    /**
     * 修改码值
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody CodeTable codeTable) {
        return Result.ok(codeTableService.updateById(codeTable));
    }

    /**
     * 删除码值
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(codeTableService.removeById(id));
    }
}
