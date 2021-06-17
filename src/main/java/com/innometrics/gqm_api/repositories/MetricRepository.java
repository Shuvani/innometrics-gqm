package com.innometrics.gqm_api.repositories;

import com.innometrics.gqm_api.model.Metric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetricRepository extends JpaRepository<Metric, Long> {

    List<Metric> findAllByNameIn(List<String> metricsNames);

}
