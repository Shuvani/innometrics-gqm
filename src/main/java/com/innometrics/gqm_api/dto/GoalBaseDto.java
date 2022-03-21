package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Goal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Details about the goal")
public class GoalBaseDto {

    @NotEmpty(message = "Content can not be empty")
    @ApiModelProperty(notes = "Content of the goal")
    private String content;

    @NotEmpty(message = "Email is required")
    @ApiModelProperty(notes = "Email of the person who created this goal")
    private String userEmail;

    public static GoalBaseDto buildFrom(Goal goal) {
        return builder()
                .content(goal.getContent())
                .userEmail(goal.getUserEmail())
                .build();
    }

}
