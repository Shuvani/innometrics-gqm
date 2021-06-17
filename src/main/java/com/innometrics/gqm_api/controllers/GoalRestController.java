package com.innometrics.gqm_api.controllers;

import com.innometrics.gqm_api.dto.GoalBaseDto;
import com.innometrics.gqm_api.dto.GoalRetrieveResponse;
import com.innometrics.gqm_api.dto.QuestionsForGoalDto;
import com.innometrics.gqm_api.service.GoalService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/goals")
public class GoalRestController {

    private final GoalService goalService;

    //    get:
    //    API endpoint that returns a list of goals assigned to the current user.

    @GetMapping("/{id}")
    @ApiOperation(
            value = "Finds goal by id",
            notes = "API endpoint that returns the goal with this primary key"
    )
    public GoalRetrieveResponse getGoal(@PathVariable("id") Long goalId){
        return goalService.getDtoById(goalId);
    }

    @GetMapping
    @ApiOperation(
            value = "Returns all goals",
            notes = "API endpoint that returns a list of all existing goals"
    )
    public List<GoalRetrieveResponse> getAllGoals(){
        return goalService.getAllDtos();
    }

    @GetMapping("/{id}/questions")
    @ApiOperation(
            value = "Returns a list of questions assigned to the goal",
            notes = "API endpoint that returns a list of all questions, assigned to the goal with given id"
    )
    public QuestionsForGoalDto getQuestions(@PathVariable("id") Long goalId) {
        return goalService.getQuestionsByGoalId(goalId);
    }

    @PostMapping
    @ApiOperation(
            value = "Create new goal",
            notes = "API endpoint to create a new goal"
    )
    public GoalBaseDto createGoal(@Valid @RequestBody GoalBaseDto goal){
        return goalService.createDtoFrom(goal);
    }

    @PutMapping("/{id}")
    @ApiOperation(
            value = "Updates the goal",
            notes = "API endpoint that updates the goal with this primary key"
    )
    public GoalBaseDto updateGoal(
            @PathVariable("id") Long goalId,
            @RequestBody GoalBaseDto goalBaseDto
    ) {
        return goalService.updateBy(goalId, goalBaseDto);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(
            value = "Deletes the goal",
            notes = "API endpoint that deletes the goal with this primary key"
    )
    public void deleteGoal(@PathVariable("id") Long goalId){
        goalService.deleteById(goalId);
    }
}
