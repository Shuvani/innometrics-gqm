package com.innometrics.gqm_api.repositories;

import com.innometrics.gqm_api.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {
}
