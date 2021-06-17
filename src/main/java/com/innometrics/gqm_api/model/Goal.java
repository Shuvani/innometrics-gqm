package com.innometrics.gqm_api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.innometrics.gqm_api.dto.GoalBaseDto;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Goal {

    public static final String GENERATOR = "GoalGenerator";

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(
            name = GENERATOR,
            sequenceName = "goal_id_seq",
            allocationSize = 1
    )
    private Long id;

    private String content;

    private String userEmail;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "goal")
    @JsonManagedReference
    private Set<Question> questions;

    public static Goal buildFrom(GoalBaseDto goalBaseDto) {
        return Goal.builder()
                   .content(goalBaseDto.getContent())
                   .userEmail(goalBaseDto.getUserEmail())
                   .questions(new HashSet<>())
                   .build();
    }

}
