package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Question;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGenerateMetricsRequest extends QuestionBaseDto {

    @ApiModelProperty(notes = "List of metrics assigned to the question")
    private List<MetricResponse> metrics;

    public static QuestionGenerateMetricsRequest buildFrom(Question question) {
        return builder()
                .content(question.getContent())
                .metrics(MetricResponse.buildFrom(question.getMetrics()))
                .build();
    }
}
