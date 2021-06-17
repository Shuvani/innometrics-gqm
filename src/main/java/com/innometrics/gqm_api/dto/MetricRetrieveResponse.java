package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Metric;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class MetricRetrieveResponse extends MetricBaseDto {

    @ApiModelProperty(notes = "Id of the metric")
    private Long id;

    @ApiModelProperty(notes = "List of questions assigned to the metric")
    private List<QuestionResponse> questions;

    public static MetricRetrieveResponse buildFrom(Metric metric) {
        return builder()
                .id(metric.getId())
                .name(metric.getName())
                .description(metric.getDescription())
                .questions(QuestionResponse.buildFrom(metric.getQuestions()))
                .build();
    }
}
