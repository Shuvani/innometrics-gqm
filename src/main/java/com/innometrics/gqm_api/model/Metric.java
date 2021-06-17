package com.innometrics.gqm_api.model;

import com.innometrics.gqm_api.dto.MetricBaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Data
@Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@NoArgsConstructor
@AllArgsConstructor
public class Metric {

    public static final String GENERATOR = "MetricGenerator";

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = GENERATOR)
    @SequenceGenerator(
            name = GENERATOR,
            sequenceName = "metric_id_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(name="name")
    private String name;

    private String description;

    @ManyToMany(mappedBy = "metrics")
    @ApiModelProperty(notes = "Set of questions related to this metric")
    private Set<Question> questions = new HashSet<>();

    public static Metric buildFrom(MetricBaseDto metricBaseDto) {
        return Metric.builder()
                .name(metricBaseDto.getName())
                .description(metricBaseDto.getDescription())
                .build();
    }

}
