package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Metric;
import com.innometrics.gqm_api.model.Question;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionUpdateMetricsRequest {

    @ApiModelProperty(notes = "List of metrics ids which are assigned to that question")
    private List<Long> metricIds;

    public static QuestionUpdateMetricsRequest buildFrom(Question question) {
        return QuestionUpdateMetricsRequest.builder()
                .metricIds(
                        question.getMetrics().stream()
                                .map(Metric::getId)
                                .collect(Collectors.toList())
                )
                .build();
    }

}
