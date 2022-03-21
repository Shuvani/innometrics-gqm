package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Question;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;


@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionWithIdDto extends QuestionBaseDto{

  @ApiModelProperty(notes = "Id of the question")
  private Long questionId;

  @NotNull
  @ApiModelProperty(notes = "Id of the goal related to the question")
  private Long goalId;

  public static QuestionWithIdDto buildFrom(Question question) {
    return builder()
      .questionId(question.getId())
      .content(question.getContent())
      .goalId(question.getGoal().getId())
      .build();
  }
}
