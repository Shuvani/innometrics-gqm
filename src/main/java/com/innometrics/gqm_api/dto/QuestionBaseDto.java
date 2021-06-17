package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Question;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Details about the question")
public class QuestionBaseDto {

    @NotEmpty
    @ApiModelProperty(notes = "The content of the question")
    private String content;

    public static QuestionBaseDto buildFrom(Question question) {
        return builder()
                .content(question.getContent())
                .build();
    }

}
