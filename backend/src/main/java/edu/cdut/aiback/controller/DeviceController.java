package edu.cdut.aiback.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.cdut.aiback.common.Result;
import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.dto.DeviceQueryDTO;
import edu.cdut.aiback.entity.Device;
import edu.cdut.aiback.service.DeviceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * 分页列表
     */
    @GetMapping("/page")
    public Result<Page<Device>> page(DeviceQueryDTO query) {
        return Result.ok(deviceService.page(query));
    }

    /**
     * 按运营类型查询设备列表（建单时选择点位用）
     */
    @GetMapping("/list-by-type")
    public Result<List<Device>> listByType(@RequestParam(required = false) String operationType) {
        return Result.ok(deviceService.listByOperationType(operationType));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public Result<Device> getById(@PathVariable Long id) {
        return Result.ok(deviceService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody Device device) {
        device.setProjectGroup(UserContext.getProjectGroup());
        return Result.ok(deviceService.save(device));
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Device device) {
        return Result.ok(deviceService.updateById(device));
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(deviceService.removeById(id));
    }

    /**
     * Excel 导入
     */
    @PostMapping("/import")
    public Result<String> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        List<Device> list = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), Device.class, new ReadListener<Device>() {
            @Override
            public void invoke(Device data, AnalysisContext context) {
                data.setProjectGroup(UserContext.getProjectGroup());
                list.add(data);
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).sheet().doRead();
        deviceService.saveBatch(list);
        return Result.ok("导入成功，共 " + list.size() + " 条");
    }

    /**
     * Excel 导出
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("设备台账.xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        List<Device> list = deviceService.list();
        EasyExcel.write(response.getOutputStream(), Device.class).sheet("设备台账").doWrite(list);
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public void template(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("设备导入模板.xlsx", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        List<Device> demoList = new ArrayList<>();
        Device demo = new Device();
        demo.setDeviceCode("CAM-001");
        demo.setDeviceName("示例摄像头");
        demo.setArea("A区");
        demo.setIp("192.168.1.100");
        demo.setCameraType("枪机");
        demo.setOperationType("巡检");
        demoList.add(demo);

        EasyExcel.write(response.getOutputStream(), Device.class).sheet("设备台账").doWrite(demoList);
    }
}
