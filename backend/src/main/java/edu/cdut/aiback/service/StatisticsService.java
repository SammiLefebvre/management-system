package edu.cdut.aiback.service;

import edu.cdut.aiback.vo.DashboardStatisticsVO;
import edu.cdut.aiback.vo.DeviceMapVO;
import edu.cdut.aiback.vo.HeatmapVO;
import edu.cdut.aiback.vo.PersonnelWorkloadVO;
import edu.cdut.aiback.vo.WorkOrderTrendVO;

import java.util.List;

public interface StatisticsService {
    DashboardStatisticsVO getDashboardStatistics();
    WorkOrderTrendVO getTrends(int days, String projectGroup);
    HeatmapVO getHeatmap(String projectGroup);
    List<PersonnelWorkloadVO> getWorkload(String projectGroup);
    List<DeviceMapVO> getDevicesWithLocation(String projectGroup);
    String summaryForAi();
}
