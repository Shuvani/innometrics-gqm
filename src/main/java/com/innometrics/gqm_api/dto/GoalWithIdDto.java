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
public class GoalWithIdDto {

  @ApiModelProperty(notes = "Id of the goal")
  private Long id;

  @ApiModelProperty(notes = "Content of the goal")
  private String content;

  @ApiModelProperty(notes = "Email of the person who created this goal")
  private String userEmail;

  public static GoalWithIdDto buildFrom(Goal goal) {
    return builder()
      .id(goal.getId())
      .content(goal.getContent())
      .userEmail(goal.getUserEmail())
      .build();
  }

}
