package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Goal;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class QuestionsForGoalDto {

    @ApiModelProperty(notes = "List of questions assigned to the goal")
    private List<QuestionResponse> questions = new ArrayList<>();

    public QuestionsForGoalDto(List<QuestionResponse> questions) {
        this.questions = questions;
    }

    public QuestionsForGoalDto() {}

    public static QuestionsForGoalDto buildFrom(Goal goal) {
        return new QuestionsForGoalDto(
                QuestionResponse.buildFrom(
                        goal.getQuestions()
                )
        );
    }

}