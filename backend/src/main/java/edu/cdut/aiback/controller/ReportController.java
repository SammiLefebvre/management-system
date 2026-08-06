package edu.cdut.aiback.controller;

import edu.cdut.aiback.common.UserContext;
import edu.cdut.aiback.dto.ReportQueryDTO;
import edu.cdut.aiback.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/export-excel")
    public void exportExcel(@RequestBody ReportQueryDTO dto, HttpServletResponse response) throws Exception {
        reportService.exportExcel(dto, UserContext.getProjectGroup(), response);
    }

    @PostMapping("/export-pdf")
    public void exportPdf(@RequestBody ReportQueryDTO dto, HttpServletResponse response) throws Exception {
        reportService.exportPdf(dto, UserContext.getProjectGroup(), response);
    }
}
