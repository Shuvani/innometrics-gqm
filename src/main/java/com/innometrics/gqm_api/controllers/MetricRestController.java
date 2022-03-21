package com.innometrics.gqm_api.controllers;

import com.innometrics.gqm_api.dto.*;
import com.innometrics.gqm_api.model.Metric;
import com.innometrics.gqm_api.service.MetricService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/metrics")
public class MetricRestController {

    private final MetricService metricService;

    @CrossOrigin
    @GetMapping("/{id}")
    @ApiOperation(value = "Finds metric by id",
            notes = "API endpoint that returns the metric with this primary key",
            response = Metric.class)
    public MetricRetrieveResponse getMetric(@PathVariable("id") Long metricId){
        return metricService.getDtoById(metricId);
    }

    @CrossOrigin
    @GetMapping
    @ApiOperation(
            value = "Returns all metrics",
            notes = "API endpoint that returns a list of all existing metric"
    )
    public List<MetricRetrieveResponse> getAllMetrics(){
        return metricService.getAllDtos();
    }

    @CrossOrigin
    @PostMapping
    @ApiOperation(
            value = "Creates new metric",
            notes = "API endpoint to create a new metric"
    )
    public MetricBaseDto createMetric(@Valid @RequestBody MetricBaseDto metric){
        return metricService.createDtoFrom(metric);
    }

    @CrossOrigin
    @PutMapping("/{id}")
    @ApiOperation(
            value = "Updates the metric",
            notes = "API endpoint that updates the metric with this primary key"
    )
    public MetricBaseDto updateMetric(
            @PathVariable("id") Long metricId,
            @RequestBody MetricBaseDto metricBaseDto
    ) {
        return metricService.updateBy(metricId, metricBaseDto);
    }

    @CrossOrigin
    @DeleteMapping("/{id}")
    @ApiOperation(
            value = "Deletes the metric",
            notes = "API endpoint that deletes the metric with this primary key"
    )
    public void deleteMetric(@PathVariable("id") Long metricId){
        metricService.deleteById(metricId);
    }

}
