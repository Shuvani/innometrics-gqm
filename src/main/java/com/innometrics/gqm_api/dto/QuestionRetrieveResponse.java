package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Question;
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
public class QuestionRetrieveResponse extends QuestionBaseDto {

    @ApiModelProperty(notes = "Id of the question")
    private Long id;

    @ApiModelProperty(notes = "Goal to which this question is assigned")
    private GoalResponse goal;

    @ApiModelProperty(notes = "List of metrics assigned to the question")
    private List<MetricResponse> metrics;

    public static QuestionRetrieveResponse buildFrom(Question question) {
        return builder()
                .id(question.getId())
                .content(question.getContent())
                .goal(GoalResponse.buildFrom(question.getGoal()))
                .metrics(MetricResponse.buildFrom(question.getMetrics()))
                .build();
    }
}
