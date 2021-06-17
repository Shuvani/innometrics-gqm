package com.innometrics.gqm_api.repositories;

import com.innometrics.gqm_api.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    Set<Question> findAllByIdIsNot(Long id);

}
