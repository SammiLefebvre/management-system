package edu.cdut.aiback.vo;

import lombok.Data;
import java.util.List;

@Data
public class WorkOrderTrendVO {
    private List<String> dates;
    private List<TrendSeries> series;

    @Data
    public static class TrendSeries {
        private String name;
        private List<Long> data;

        public TrendSeries(String name, List<Long> data) {
            this.name = name;
            this.data = data;
        }
    }
}
