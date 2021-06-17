package com.innometrics.gqm_api.controllers;

import com.innometrics.gqm_api.dto.ApiErrorResponse;
import com.innometrics.gqm_api.dto.MetricBaseDto;
import com.innometrics.gqm_api.dto.MetricRetrieveResponse;
import com.innometrics.gqm_api.model.Metric;
import com.innometrics.gqm_api.model.Question;
import com.innometrics.gqm_api.repositories.MetricRepository;
import com.innometrics.gqm_api.repositories.QuestionRepository;
import lombok.NoArgsConstructor;
import lombok.val;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@NoArgsConstructor
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MetricRestControllerTest {

    private static final String API_METRICS = "/api/metrics";

    @Autowired
    TestRestTemplate testRestTemplate;
    @Autowired
    MetricRepository metricRepository;
    @Autowired
    QuestionRepository questionRepository;

    @BeforeEach
    public void cleanUp() {
        metricRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    //getMetric tests
    @Test
    public void getMetric_whenMetricIsNotInDB_receiveNotFound() {
        ResponseEntity<Object> response = testRestTemplate.getForEntity(API_METRICS + "/1", Object.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getMetric_whenMetricIsInDB_receiveOK() {
        //given
        Metric metric = createValidMetric("test1 metric", "this is test1 metric");
        metricRepository.save(metric);

        //when
        val response = testRestTemplate.getForEntity(
                API_METRICS + "/" + metric.getId(),
                MetricRetrieveResponse.class
        );

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getMetric_whenMetricIsInDB_receiveNotNull() {
        //given
        Metric metric = createValidMetric("test1 metric", "this is test1 metric");
        metricRepository.save(metric);

        //when
        val response = testRestTemplate.getForEntity(
                API_METRICS + "/" + metric.getId(),
                MetricRetrieveResponse.class
        );

        //then
        assertNotNull(response.getBody());
    }

    @Test
    public void getMetric_whenMetricIsInDB_receiveMetricRetrieveResponse() {
        //given
        Metric metric = createValidMetric("test1 metric", "this is test1 metric");
        metric.setQuestions(new HashSet<>());
        metricRepository.save(metric);

        //when
        val response = testRestTemplate.getForEntity(
                API_METRICS + "/" + metric.getId(),
                MetricRetrieveResponse.class
        );

        //then
        assertEquals(MetricRetrieveResponse.buildFrom(metric), response.getBody());
    }

    //getAllMetrics tests
    @Test
    public void getAllMetrics_whenMetricsAreNotInDB_receiveZeroItems() {
        val response = testRestTemplate.getForEntity(API_METRICS, List.class);
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    public void getAllMetrics_whenMetricsAreInDB_receiveOK() {
        metricRepository.save(createValidMetric("test1 metric", "this is test1 metric"));
        ResponseEntity<Object> response = testRestTemplate.getForEntity(API_METRICS, Object.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getAllGoals_whenGoalsAreInDB_receiveListOfGoals() {
        //given
        metricRepository.save(createValidMetric("test1 metric", "this is test1 metric"));
        metricRepository.save(createValidMetric("test2 metric", "this is test2 metric"));

        //when
        val response = testRestTemplate.getForEntity(
                API_METRICS,
                List.class
        );

        //then
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    //createMetric tests
    @Test
    public void createMetric_whenMetricIsValid_receiveOk() {
        Metric metric = createValidMetric("test1 metric", "this is test1 metric");
        ResponseEntity<Object> response = testRestTemplate.postForEntity(API_METRICS, metric, Object.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void createMetric_whenMetricIsValid_metricSavedToDatabase() {
        Metric metric = createValidMetric("test1 metric", "this is test1 metric");
        testRestTemplate.postForEntity(API_METRICS, metric, Object.class);
        assertEquals(1, metricRepository.count());
    }

    @Test
    public void createMetric_whenMetricAlreadyExists_receiveBadRequest() {
        metricRepository.save(createValidMetric("test1 metric", "this is test1 metric"));
        Metric metric = createValidMetric("test1 metric", "this is test1 metric");
        ResponseEntity<ApiErrorResponse> response = testRestTemplate.postForEntity(API_METRICS, metric, ApiErrorResponse.class);
//        assertEquals(1, metricRepository.count());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void createMetric_whenMetricIsValid_receiveNotNull() {
        Metric metric = createValidMetric("test1 metric", "this is test1 metric");
        ResponseEntity<MetricBaseDto> response = testRestTemplate.postForEntity(
                API_METRICS,
                metric,
                MetricBaseDto.class
        );
        assertNotNull(response.getBody());
    }

    @Test
    public void createMetric_whenMetricIsValid_receiveMetricBaseDto() {
        Metric metric = createValidMetric("test1 metric", "this is test1 metric");
        ResponseEntity<MetricBaseDto> response = testRestTemplate.postForEntity(
                API_METRICS,
                metric,
                MetricBaseDto.class
        );
        assertEquals(MetricBaseDto.buildFrom(metric), response.getBody());
    }

    @Test
    public void createMetric_whenNameIsEmpty_receiveBadRequest() {
        Metric metric = createValidMetric("test1 metric", "this is test1 metric");
        metric.setName(null);
        ResponseEntity<Object> response = testRestTemplate.postForEntity(API_METRICS, metric, Object.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //deleteMetric tests
    @Test
    public void deleteMetric_whenMetricIsNotInDb_receiveForbidden() {
        ResponseEntity<Object> response = testRestTemplate.exchange(
                API_METRICS + "/1",
                HttpMethod.DELETE,
                null,
                Object.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void deleteMetric_whenMetricIsInDb_receiveOK() {
        Metric metric = metricRepository.save(
                createValidMetric("test1 metric", "this is test1 metric")
        );
        ResponseEntity<Object> response = testRestTemplate.exchange(
                API_METRICS + "/" + metric.getId(),
                HttpMethod.DELETE,
                null,
                Object.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void deleteMetric_whenMetricIsInDb_goalRemovedFromDb() {
        Metric metric = metricRepository.save(
                createValidMetric("test1 metric", "this is test1 metric")
        );
        testRestTemplate.exchange(
                API_METRICS + "/" + metric.getId(),
                HttpMethod.DELETE,
                null,
                Object.class
        );
        Optional<Metric> inDb = metricRepository.findById(metric.getId());
        assertFalse(inDb.isPresent());
    }

//    @Test
//    public void deleteMetric_whenMetricHasQuestions_questionsNotRemovedFromDb() {
//        //given
//        Metric metric = metricRepository.save(
//                createValidMetric("test1 metric", "this is test1 metric")
//        );
//        Question question = questionRepository.save(createQuestion(metric));
//
//        //when
//        testRestTemplate.exchange(
//                API_METRICS + "/" + metric.getId(),
//                HttpMethod.DELETE,
//                null,
//                Object.class
//        );
//
//        //then
//        Optional<Question> optionalQuestion = questionRepository.findById(question.getId());
//        assertTrue(optionalQuestion.isPresent());
//    }

    private Metric createValidMetric(String name, String description) {
        val metricBaseDto = MetricBaseDto.builder()
                                         .name(name)
                                         .description(description)
                                         .build();
        return Metric.buildFrom(metricBaseDto);
    }

    private Question createQuestion(Metric metric) {
        HashSet<Metric> metrics = new HashSet<>();
        metrics.add(metric);
        return Question.builder()
                       .content("question content")
                       .metrics(metrics)
                       .build();
    }

}
