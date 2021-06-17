package com.innometrics.gqm_api.service;

import com.innometrics.gqm_api.dto.MetricBaseDto;
import com.innometrics.gqm_api.dto.MetricRetrieveResponse;
import com.innometrics.gqm_api.exception.ForbiddenException;
import com.innometrics.gqm_api.exception.NotFoundException;
import com.innometrics.gqm_api.model.Metric;
import com.innometrics.gqm_api.repositories.MetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MetricRepository metricRepository;

    public Metric save(Metric metric) {
        return metricRepository.save(metric);
    }

    @Transactional
    public MetricRetrieveResponse getDtoById(Long metricId) {
        return MetricRetrieveResponse.buildFrom(
                getMetricById(metricId, new NotFoundException("Metric not found by id " + metricId))
        );
    }

    @Transactional
    public Metric getMetricById(Long metricId, RuntimeException exception) {
        return metricRepository.findById(metricId).orElseThrow(
                () -> exception
        );
    }

    public List<MetricRetrieveResponse> getAllDtos() {
        return getAll().stream()
                .map(MetricRetrieveResponse::buildFrom)
                .collect(Collectors.toList());
    }

    public List<Metric> getAll() {
        return metricRepository.findAll();
    }

    public MetricBaseDto createDtoFrom(MetricBaseDto metric) {
        val resultMetric = Metric.buildFrom(metric);
        return MetricBaseDto.buildFrom(save(resultMetric));
    }

    @Transactional
    public MetricBaseDto updateBy(Long metricId, MetricBaseDto metricBaseDto) {
        val updatable = getMetricById(metricId, new NotFoundException("Metric not found by id " + metricId));
        updateFields(updatable, Metric.buildFrom(metricBaseDto));
        return MetricBaseDto.buildFrom(save(updatable));
    }

    private void updateFields(Metric updatable, Metric updater) {
        updatable.setName(updater.getName());
        updatable.setDescription(updater.getDescription());
    }

    public void deleteById(Long metricId) {
        getMetricById(metricId, new ForbiddenException("Metric with id  " + metricId + "does not exist"));
        metricRepository.deleteById(metricId);
    }

    public List<Metric> getAllByIds(List<Long> metricIds) {
        return metricRepository.findAllById(metricIds);
    }
}
