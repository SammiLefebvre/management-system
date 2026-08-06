package edu.cdut.aiback.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import edu.cdut.aiback.dto.ReportQueryDTO;
import edu.cdut.aiback.entity.Device;
import edu.cdut.aiback.entity.Personnel;
import edu.cdut.aiback.entity.WorkOrder;
import edu.cdut.aiback.mapper.DeviceMapper;
import edu.cdut.aiback.mapper.PersonnelMapper;
import edu.cdut.aiback.mapper.WorkOrderMapper;
import edu.cdut.aiback.vo.export.DeviceExportRow;
import edu.cdut.aiback.vo.export.PersonnelExportRow;
import edu.cdut.aiback.vo.export.WorkOrderExportRow;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final WorkOrderMapper workOrderMapper;
    private final DeviceMapper deviceMapper;
    private final PersonnelMapper personnelMapper;

    public void exportExcel(ReportQueryDTO dto, String projectGroup, HttpServletResponse response) throws Exception {
        String filename = dto.getDataType() + "_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        OutputStream out = response.getOutputStream();
        switch (dto.getDataType()) {
            case "work_order" -> EasyExcel.write(out, WorkOrderExportRow.class).sheet("工单").doWrite(buildWorkOrderRows(dto, projectGroup));
            case "device" -> EasyExcel.write(out, DeviceExportRow.class).sheet("设备").doWrite(buildDeviceRows(dto, projectGroup));
            case "personnel" -> EasyExcel.write(out, PersonnelExportRow.class).sheet("人员").doWrite(buildPersonnelRows(dto, projectGroup));
            default -> throw new IllegalArgumentException("未知数据类型");
        }
        out.flush();
    }

    public void exportPdf(ReportQueryDTO dto, String projectGroup, HttpServletResponse response) throws Exception {
        String filename = dto.getDataType() + "_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        String html = buildHtml(dto, projectGroup);
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(response.getOutputStream());
        builder.run();
    }

    private List<WorkOrderExportRow> buildWorkOrderRows(ReportQueryDTO dto, String projectGroup) {
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkOrder::getProjectGroup, projectGroup);
        if (dto.getStatus() != null) wrapper.eq(WorkOrder::getStatus, dto.getStatus());
        if (dto.getStartDate() != null) wrapper.ge(WorkOrder::getPublishTime, LocalDate.parse(dto.getStartDate()).atStartOfDay());
        if (dto.getEndDate() != null) wrapper.le(WorkOrder::getPublishTime, LocalDate.parse(dto.getEndDate()).atTime(23, 59, 59));
        List<WorkOrder> list = workOrderMapper.selectList(wrapper);
        return list.stream().map(w -> {
            WorkOrderExportRow r = new WorkOrderExportRow();
            r.setWorkOrderCode(w.getWorkOrderCode());
            Device d = deviceMapper.selectById(w.getDeviceId());
            r.setDeviceCode(d != null ? d.getDeviceCode() : "");
            r.setFaultType(w.getFaultType());
            r.setEmergencyLevel(w.getEmergencyLevel());
            r.setStatus(w.getStatus());
            r.setPublishTime(w.getPublishTime() != null ? w.getPublishTime().toString() : "");
            r.setCompleteTime(w.getCompleteTime() != null ? w.getCompleteTime().toString() : "");
            return r;
        }).toList();
    }

    private List<DeviceExportRow> buildDeviceRows(ReportQueryDTO dto, String projectGroup) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getProjectGroup, projectGroup);
        List<Device> list = deviceMapper.selectList(wrapper);
        return list.stream().map(d -> {
            DeviceExportRow r = new DeviceExportRow();
            r.setDeviceCode(d.getDeviceCode());
            r.setDeviceName(d.getDeviceName());
            r.setArea(d.getArea());
            r.setIp(d.getIp());
            r.setProjectGroup(d.getProjectGroup());
            return r;
        }).toList();
    }

    private List<PersonnelExportRow> buildPersonnelRows(ReportQueryDTO dto, String projectGroup) {
        LambdaQueryWrapper<Personnel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Personnel::getProjectGroup, projectGroup);
        if (dto.getStatus() != null) wrapper.eq(Personnel::getStatus, dto.getStatus());
        List<Personnel> list = personnelMapper.selectList(wrapper);
        return list.stream().map(p -> {
            PersonnelExportRow r = new PersonnelExportRow();
            r.setAccount(p.getAccount());
            r.setName(p.getName());
            r.setPhone(p.getPhone());
            r.setRole(p.getRole());
            r.setProjectGroup(p.getProjectGroup());
            r.setStatus(p.getStatus() != null && p.getStatus() == 1 ? "启用" : "禁用");
            return r;
        }).toList();
    }

    private String buildHtml(ReportQueryDTO dto, String projectGroup) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>table{width:100%;border-collapse:collapse;}th,td{border:1px solid #ccc;padding:8px;text-align:left;}</style></head><body>");
        sb.append("<h1>报表</h1>");
        sb.append("<p>项目组：").append(projectGroup).append("</p>");
        sb.append("<p>数据类型：").append(dto.getDataType()).append("</p>");
        sb.append("<p>时间范围：").append(dto.getStartDate()).append(" ~ ").append(dto.getEndDate()).append("</p>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
