package com.innometrics.gqm_api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.innometrics.gqm_api.dto.QuestionBaseDto;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.SEQUENCE;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    public static final String GENERATOR = "QuestionGenerator";

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(
            name = GENERATOR,
            sequenceName = "question_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JsonBackReference
    private Goal goal;

    @ManyToMany
    @JoinTable(
            name = "question_metric",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "metric_id")
    )
    private Set<Metric> metrics = new HashSet<>();

    public static Question buildFrom(QuestionBaseDto questionBaseDto) {
        return Question.builder()
                .content(questionBaseDto.getContent())
                .build();
    }
}
