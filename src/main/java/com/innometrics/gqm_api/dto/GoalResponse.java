package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Goal;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoalResponse {

    @ApiModelProperty(notes = "Id of the goal")
    private final Long id;

    @ApiModelProperty(notes = "Content of the goal")
    private final String content;

    public static GoalResponse buildFrom(Goal goal) {
        return builder()
                .id(goal.getId())
                .content(goal.getContent())
                .build();
    }
}
