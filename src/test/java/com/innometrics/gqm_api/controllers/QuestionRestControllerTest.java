package com.innometrics.gqm_api.controllers;

import com.innometrics.gqm_api.dto.*;
import com.innometrics.gqm_api.model.Goal;
import com.innometrics.gqm_api.model.Metric;
import com.innometrics.gqm_api.model.Question;
import com.innometrics.gqm_api.repositories.GoalRepository;
import com.innometrics.gqm_api.repositories.MetricRepository;
import com.innometrics.gqm_api.repositories.QuestionRepository;
import lombok.NoArgsConstructor;
import lombok.val;
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

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@NoArgsConstructor
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuestionRestControllerTest {

    private static final String API_QUESTIONS = "/api/questions";

    @Autowired
    TestRestTemplate testRestTemplate;
    @Autowired
    GoalRepository goalRepository;
    @Autowired
    MetricRepository metricRepository;
    @Autowired
    QuestionRepository questionRepository;

    @BeforeEach
    public void cleanUp() {
        questionRepository.deleteAll();
        goalRepository.deleteAll();
        metricRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }

    //getQuestion tests
    @Test
    public void getQuestion_whenQuestionIsNotInDB_receiveNotFound() {
        ResponseEntity<Object> response = testRestTemplate.getForEntity(API_QUESTIONS + "/1", Object.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getQuestion_whenQuestionIsInDB_receiveOK() {
        //given
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Metric metric = metricRepository.save(
                createValidMetric("test1 metric", "this is test1 metric")
        );
        Question question = questionRepository.save(createQuestionWithMetrics(goal, metric));

        //when
        val response = testRestTemplate.getForEntity(
                API_QUESTIONS + "/" + question.getId(),
                QuestionRetrieveResponse.class
        );

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getQuestion_whenQuestionIsInDB_receiveNotNull() {
        //given
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Metric metric = metricRepository.save(
                createValidMetric("test1 metric", "this is test1 metric")
        );
        Question question = questionRepository.save(createQuestionWithMetrics(goal, metric));

        //when
        val response = testRestTemplate.getForEntity(
                API_QUESTIONS + "/" + question.getId(),
                QuestionRetrieveResponse.class
        );

        //then
        assertNotNull(response.getBody());
    }

    @Test
    public void getQuestion_whenQuestionIsInDB_receiveQuestionRetrieveResponse() {
        //given
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Metric metric = metricRepository.save(
                createValidMetric("test1 metric", "this is test1 metric")
        );
        Question question = questionRepository.save(createQuestionWithMetrics(goal, metric));

        //when
        ResponseEntity<QuestionRetrieveResponse> response = testRestTemplate.getForEntity(
                API_QUESTIONS + "/" + question.getId(),
                QuestionRetrieveResponse.class
        );

        //then
        assertEquals(QuestionRetrieveResponse.buildFrom(question), response.getBody());
    }

    //getAllQuestions tests
    @Test
    public void getAllQuestions_whenQuestionsAreNotInDB_receiveZeroItems() {
        val response = testRestTemplate.getForEntity(API_QUESTIONS, List.class);
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    public void getAllQuestions_whenQuestionsAreInDB_receiveOK() {
        //given
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Metric metric = metricRepository.save(
                createValidMetric("test1 metric", "this is test1 metric")
        );
        Question question = questionRepository.save(createQuestionWithMetrics(goal, metric));

        ResponseEntity<Object> response = testRestTemplate.getForEntity(API_QUESTIONS, Object.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getAllQuestions_whenQuestionsAreInDB_receiveListOfQuestions() {
        //given
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Metric metric = metricRepository.save(
                createValidMetric("test1 metric", "this is test1 metric")
        );
        Question question1 = questionRepository.save(createQuestionWithMetrics(goal, metric));
        Question question2 = createQuestionWithMetrics(goal, metric);
        question2.setContent("question2");
        questionRepository.save(question2);

        //when
        val response = testRestTemplate.getForEntity(
                API_QUESTIONS,
                List.class
        );

        //then
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    //createQuestion tests
    @Test
    public void createQuestion_whenQuestionIsValid_receiveOk() {
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Question question = questionRepository.save(createValidQuestion(goal));

        ResponseEntity<Object> response = testRestTemplate.postForEntity(
                API_QUESTIONS,
                QuestionCreateUpdateRequest.buildFrom(question),
                Object.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void createQuestion_whenQuestionIsValid_questionSavedToDatabase() {
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Question question = createValidQuestion(goal);

        testRestTemplate.postForEntity(
                API_QUESTIONS,
                QuestionCreateUpdateRequest.buildFrom(question),
                Object.class
        );

        assertEquals(1, questionRepository.count());
    }

    @Test
    public void createQuestion_whenQuestionIsValid_receiveNotNull() {
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Question question = createValidQuestion(goal);

        ResponseEntity<QuestionCreateUpdateRequest> response = testRestTemplate.postForEntity(
                API_QUESTIONS,
                QuestionCreateUpdateRequest.buildFrom(question),
                QuestionCreateUpdateRequest.class
        );

        assertNotNull(response.getBody());
    }

    @Test
    public void createQuestion_whenQuestionIsValid_receiveQuestionCreateUpdateRequest() {
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Question question = createValidQuestion(goal);

        ResponseEntity<QuestionCreateUpdateRequest> response = testRestTemplate.postForEntity(
                API_QUESTIONS,
                QuestionCreateUpdateRequest.buildFrom(question),
                QuestionCreateUpdateRequest.class
        );

        assertEquals(QuestionCreateUpdateRequest.buildFrom(question), response.getBody());
    }

    @Test
    public void createQuestion_whenContentIsEmpty_receiveBadRequest() {
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Question question = createValidQuestion(goal);
        question.setContent(null);

        ResponseEntity<Object> response = testRestTemplate.postForEntity(API_QUESTIONS, question, Object.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void createQuestion_whenGoalIsNull_receiveBadRequest() {
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Question question = createValidQuestion(goal);
        question.setGoal(null);

        ResponseEntity<Object> response = testRestTemplate.postForEntity(API_QUESTIONS, question, Object.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void createQuestion_whenQuestionIsInvalid_receiveErrorWithValidationErrors() {
        Question question = new Question();

        ResponseEntity<ApiErrorResponse> response = testRestTemplate.postForEntity(
                API_QUESTIONS,
                question,
                ApiErrorResponse.class
        );

        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getValidationErrors().size());
    }

    //deleteQuestion tests
    @Test
    public void deleteQuestion_whenQuestionIsNotInDb_receiveForbidden() {
        ResponseEntity<Object> response = testRestTemplate.exchange(
                API_QUESTIONS + "/1",
                HttpMethod.DELETE,
                null,
                Object.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void deleteQuestion_whenQuestionIsInDb_receiveOK() {
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Question question = questionRepository.save(createValidQuestion(goal));

        ResponseEntity<Object> response = testRestTemplate.exchange(
                API_QUESTIONS + "/" + question.getId(),
                HttpMethod.DELETE,
                null,
                Object.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void deleteQuestion_whenQuestionIsInDb_questionRemovedFromDb() {
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Question question = questionRepository.save(createValidQuestion(goal));

        testRestTemplate.exchange(
                API_QUESTIONS + "/" + question.getId(),
                HttpMethod.DELETE,
                null,
                Object.class
        );

        Optional<Question> inDb = questionRepository.findById(question.getId());
        assertFalse(inDb.isPresent());
    }

    @Test
    public void deleteQuestion_whenQuestionHasGoals_goalsNotRemovedFromDb() {
        //given
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Question question = questionRepository.save(createValidQuestion(goal));

        //when
        testRestTemplate.exchange(
                API_QUESTIONS + "/" + question.getId(),
                HttpMethod.DELETE,
                null,
                Object.class
        );

        //then
        Optional<Goal> optionalGoal = goalRepository.findById(goal.getId());
        assertTrue(optionalGoal.isPresent());
    }

    @Test
    public void deleteQuestion_whenQuestionHasMetrics_metricsNotRemovedFromDb() {
        //given
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Metric metric = metricRepository.save(
                createValidMetric("test1 metric", "this is test1 metric")
        );
        Question question = questionRepository.save(createQuestionWithMetrics(goal, metric));

        //when
        testRestTemplate.exchange(
                API_QUESTIONS + "/" + question.getId(),
                HttpMethod.DELETE,
                null,
                Object.class
        );

        //then
        assertEquals(1, metricRepository.count());
    }

    private Goal createValidGoal(String content, String mail) {
        val goalBaseDto = GoalBaseDto.builder()
                                     .content(content)
                                     .userEmail(mail)
                                     .build();
        return Goal.buildFrom(goalBaseDto);
    }

    private Metric createValidMetric(String name, String description) {
        val metricBaseDto = MetricBaseDto.builder()
                                         .name(name)
                                         .description(description)
                                         .build();
        return Metric.buildFrom(metricBaseDto);
    }

    private Question createValidQuestion(Goal goal) {
        return Question.builder()
                       .content("question content")
                       .goal(goal)
                       .build();
    }

    private Question createQuestionWithMetrics(Goal goal, Metric metric) {
        return Question.builder()
                       .content("question content")
                       .goal(goal)
                       .metrics(new HashSet<>(asList(metric)))
                       .build();
    }

}
