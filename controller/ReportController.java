package com.example.demo.controller;

import com.example.demo.dto.DashboardReportDTO;
import com.example.demo.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardReportDTO> getDashboardReport() {
        return ResponseEntity.ok(reportService.getDashboardReport());
    }
}
