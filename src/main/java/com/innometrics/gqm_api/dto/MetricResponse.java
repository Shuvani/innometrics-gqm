package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Metric;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
public class MetricResponse {

    @ApiModelProperty(notes = "Id of the metric")
    private Long id;

    @ApiModelProperty(notes = "Name of the metric")
    private String name;

    public static MetricResponse buildFrom(Metric metric) {
        return builder()
                .id(metric.getId())
                .name(metric.getName())
                .build();
    }

    public static List<MetricResponse> buildFrom(Collection<Metric> metricList) {
        return metricList.stream()
                .map(MetricResponse::buildFrom)
                .collect(Collectors.toList());
    }
}
