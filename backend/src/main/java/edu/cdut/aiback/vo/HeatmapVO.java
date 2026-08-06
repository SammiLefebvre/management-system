package edu.cdut.aiback.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class HeatmapVO {
    @JsonProperty("xAxis")
    private List<String> xAxis;

    @JsonProperty("yAxis")
    private List<String> yAxis;

    private List<int[]> data;
}
