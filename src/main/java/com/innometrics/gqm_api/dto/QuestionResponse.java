package com.innometrics.gqm_api.dto;

import com.innometrics.gqm_api.model.Question;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@SuperBuilder
@ToString
@NoArgsConstructor
public class QuestionResponse extends QuestionBaseDto {

    @ApiModelProperty(notes = "Id of the question")
    private Long id;

    public static QuestionResponse buildFrom(Question question) {
        return builder()
                .id(question.getId())
                .content(question.getContent())
                .build();
    }

    public static List<QuestionResponse> buildFrom(Collection<Question> questionList) {
        return questionList.stream()
                .map(QuestionResponse::buildFrom)
                .collect(Collectors.toList());
    }

}
