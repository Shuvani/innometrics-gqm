package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Metric;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Details about the metric")
public class MetricBaseDto {

    @NotEmpty(message = "Name is required")
    @ApiModelProperty(notes = "Name of the metric")
    private String name;

    @ApiModelProperty(notes = "Description for the metric")
    private String description;

    public static MetricBaseDto buildFrom(Metric metric) {
        return builder()
                .name(metric.getName())
                .description(metric.getDescription())
                .build();
    }

}
