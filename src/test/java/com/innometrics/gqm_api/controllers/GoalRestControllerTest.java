package com.innometrics.gqm_api.controllers;

import com.innometrics.gqm_api.dto.*;
import com.innometrics.gqm_api.model.Goal;
import com.innometrics.gqm_api.model.Question;
import com.innometrics.gqm_api.repositories.GoalRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@NoArgsConstructor
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GoalRestControllerTest {

    private static final String API_GOALS = "/api/goals";

    @Autowired
    TestRestTemplate testRestTemplate;
    @Autowired
    GoalRepository goalRepository;
    @Autowired
    QuestionRepository questionRepository;

    @BeforeEach
    public void cleanUp() {
        goalRepository.deleteAll();
        questionRepository.deleteAll();
        testRestTemplate.getRestTemplate().getInterceptors().clear();
    }


    //getGoal tests
    @Test
    public void getGoal_whenGoalIsNotInDB_receiveNotFound() {
        ResponseEntity<Object> response = testRestTemplate.getForEntity(API_GOALS + "/1", Object.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getGoal_whenGoalIsInDB_receiveOK() {
        //given
        Goal goal = createValidGoal("test1 content", "test1@mail.ru");
        goalRepository.save(goal);

        //when
        val response = testRestTemplate.getForEntity(
                API_GOALS + "/" + goal.getId(),
                GoalRetrieveResponse.class
        );

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getGoal_whenGoalIsInDB_receiveNotNull() {
        //given
        Goal goal = createValidGoal("test1 content", "test1@mail.ru");
        goalRepository.save(goal);

        //when
        val response = testRestTemplate.getForEntity(
                API_GOALS + "/" + goal.getId(),
                GoalRetrieveResponse.class
        );

        //then
        assertNotNull(response.getBody());
    }

    @Test
    public void getGoal_whenGoalIsInDB_receiveGoalRetrieveResponse() {
        //given
        Goal goal = createValidGoal("test1 content", "test1@mail.ru");
        goalRepository.save(goal);

        //when
        val response = testRestTemplate.getForEntity(
                API_GOALS + "/" + goal.getId(),
                GoalRetrieveResponse.class
        );

        //then
        assertEquals(GoalRetrieveResponse.buildFrom(goal), response.getBody());
    }

    //getAllGoals tests
    @Test
    public void getAllGoals_whenGoalsAreNotInDB_receiveZeroItems() {
        val response = testRestTemplate.getForEntity(API_GOALS, List.class);
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    public void getAllGoals_whenGoalsAreInDB_receiveOK() {
        goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        ResponseEntity<Object> response = testRestTemplate.getForEntity(API_GOALS, Object.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getAllGoals_whenGoalsAreInDB_receiveListOfGoals() {
        //given
        goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        goalRepository.save(createValidGoal("test2 content", "test2@mail.ru"));

        //when
        val response = testRestTemplate.getForEntity(
                API_GOALS,
                List.class
        );

        //then
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    //getQuestions tests
    @Test
    public void getQuestions_whenGoalIsNotInDb_receiveNotFound() {
        val response = testRestTemplate.getForEntity(
                API_GOALS + "/1/questions",
                Object.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getQuestions_whenGoalHasNoQuestions_receiveEmpty() {
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        val response = testRestTemplate.getForEntity(
                API_GOALS + "/" + goal.getId() + "/questions",
                QuestionsForGoalDto.class
        );
        assertEquals(new QuestionsForGoalDto(), response.getBody());
    }

    @Test
    public void getQuestions_whenGoalHasOneQuestion_receiveOneItem() {
        //given
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        questionRepository.save(createQuestion(goal));

        //when
        val response = testRestTemplate.getForEntity(
                API_GOALS + "/" + goal.getId() + "/questions",
                QuestionsForGoalDto.class
        );

        //then
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getQuestions());
        assertEquals(1, response.getBody().getQuestions().size());
    }

    //createGoal tests
    @Test
    public void createGoal_whenGoalIsValid_receiveOk() {
        Goal goal = createValidGoal("test1 content", "test1@mail.ru");
        ResponseEntity<Object> response = testRestTemplate.postForEntity(API_GOALS, goal, Object.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void createGoal_whenGoalIsValid_goalSavedToDatabase() {
        Goal goal = createValidGoal("test1 content", "test1@mail.ru");
        testRestTemplate.postForEntity(API_GOALS, goal, Object.class);
        assertEquals(1, goalRepository.count());
    }

    @Test
    public void createGoal_whenGoalIsValid_receiveNotNull() {
        Goal goal = createValidGoal("test1 content", "test1@mail.ru");
        ResponseEntity<GoalBaseDto> response = testRestTemplate.postForEntity(API_GOALS, goal, GoalBaseDto.class);
        assertNotNull(response.getBody());
    }

    @Test
    public void createGoal_whenGoalIsValid_receiveGoalBaseDto() {
        Goal goal = createValidGoal("test1 content", "test1@mail.ru");
        ResponseEntity<GoalBaseDto> response = testRestTemplate.postForEntity(API_GOALS, goal, GoalBaseDto.class);
        assertEquals(GoalBaseDto.buildFrom(goal), response.getBody());
    }

    @Test
    public void createGoal_whenContentIsEmpty_receiveBadRequest() {
        Goal goal = createValidGoal("test1 content", "test1@mail.ru");
        goal.setContent(null);
        ResponseEntity<Object> response = testRestTemplate.postForEntity(API_GOALS, goal, Object.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void createGoal_whenEmailIsEmpty_receiveBadRequest() {
        Goal goal = createValidGoal("test1 content", "test1@mail.ru");
        goal.setUserEmail(null);
        ResponseEntity<Object> response = testRestTemplate.postForEntity(API_GOALS, goal, Object.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void createGoal_whenEmailIsInvalid_receiveBadRequest() {
        Goal goal = createValidGoal("test1 content", "test1@mail.ru");
        goal.setUserEmail("notValidEmail");
        ResponseEntity<Object> response = testRestTemplate.postForEntity(API_GOALS, goal, Object.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void createGoal_whenGoalIsInvalid_receiveErrorWithValidationErrors() {
        Goal goal = new Goal();
        ResponseEntity<ApiErrorResponse> response = testRestTemplate.postForEntity(
                API_GOALS,
                goal,
                ApiErrorResponse.class
        );
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getValidationErrors().size());
    }

    //updateGoal tests

    //deleteGoal tests
    @Test
    public void deleteGoal_whenGoalIsNotInDb_receiveForbidden() {
        ResponseEntity<Object> response = testRestTemplate.exchange(
                API_GOALS + "/1",
                HttpMethod.DELETE,
                null,
                Object.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void deleteGoal_whenGoalIsInDb_receiveOK() {
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        ResponseEntity<Object> response = testRestTemplate.exchange(
                API_GOALS + "/" + goal.getId(),
                HttpMethod.DELETE,
                null,
                Object.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void deleteGoal_whenGoalIsInDb_goalRemovedFromDb() {
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        testRestTemplate.exchange(
                API_GOALS + "/" + goal.getId(),
                HttpMethod.DELETE,
                null,
                Object.class
        );
        Optional<Goal> inDb = goalRepository.findById(goal.getId());
        assertFalse(inDb.isPresent());
    }

    @Test
    public void deleteGoal_whenGoalHasQuestions_questionsRemovedFromDb() {
        //given
        Goal goal = goalRepository.save(createValidGoal("test1 content", "test1@mail.ru"));
        Question question = questionRepository.save(createQuestion(goal));

        //when
        testRestTemplate.exchange(
                API_GOALS + "/" + goal.getId(),
                HttpMethod.DELETE,
                null,
                Object.class
        );

        //then
        Optional<Question> optionalQuestion = questionRepository.findById(question.getId());
        assertFalse(optionalQuestion.isPresent());
    }

    private Goal createValidGoal(String content, String mail) {
        val goalBaseDto = GoalBaseDto.builder()
                   .content(content)
                   .userEmail(mail)
                   .build();
        return Goal.buildFrom(goalBaseDto);
    }

    private Question createQuestion(Goal goal) {
        return Question.builder()
                       .content("question content")
                       .goal(goal)
                       .build();
    }
}