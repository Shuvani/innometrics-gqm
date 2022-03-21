package com.innometrics.gqm_api.repositories;

import com.innometrics.gqm_api.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {

  List<Goal> findByUserEmail(String userEmail);

}
