package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Goal;
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
public class GoalRetrieveResponse extends GoalBaseDto {

    @ApiModelProperty(notes = "Id of the goal")
    private Long id;

    @ApiModelProperty(notes = "List of questions assigned to the goal")
    private List<QuestionResponse> questions;

    public static GoalRetrieveResponse buildFrom(Goal goal) {
        return builder()
                .id(goal.getId())
                .content(goal.getContent())
                .userEmail(goal.getUserEmail())
                .questions(QuestionResponse.buildFrom(goal.getQuestions()))
                .build();
    }

}
