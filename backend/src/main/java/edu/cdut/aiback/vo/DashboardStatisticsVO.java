package edu.cdut.aiback.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardStatisticsVO {
    private Long workOrderTotal;
    private Long workOrderToday;
    private Map<String, Long> statusCounts;
    private Long slaOverdueCount;
    private Long deviceTotal;
    private Long personnelTotal;
    private Long teamTotal;
    private List<DailyTrend> last7Days;

    @Data
    public static class DailyTrend {
        private String date;
        private Long created;
        private Long completed;
    }
}
