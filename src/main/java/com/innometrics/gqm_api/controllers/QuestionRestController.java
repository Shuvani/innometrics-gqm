package com.innometrics.gqm_api.controllers;

import com.innometrics.gqm_api.dto.*;
import com.innometrics.gqm_api.service.QuestionService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionRestController {

    private final QuestionService questionService;

    @CrossOrigin
    @GetMapping("/{id}")
    @ApiOperation(
            value = "Finds question by id",
            notes = "API endpoint that returns the question with this primary key"
    )
    public QuestionRetrieveResponse getQuestion(@PathVariable("id") Long questionId){
        return questionService.getDtoById(questionId);
    }

    @CrossOrigin
    @GetMapping
    @ApiOperation(
            value = "Returns all questions",
            notes = "API endpoint that returns a list of all existing questions"
    )
    public List<QuestionRetrieveResponse> getAllQuestions(){
        return questionService.getAllDtos();
    }

    //get metrics

    @CrossOrigin
    @PostMapping
    @ApiOperation(
            value = "Create new question",
            notes = "API endpoint to create a new question"
    )
    public QuestionWithIdDto createQuestion(@Valid @RequestBody QuestionCreateUpdateRequest question){
        return questionService.createDtoFrom(question);
    }

    @CrossOrigin
    @PutMapping("/{id}")
    @ApiOperation(
            value = "Updates the question",
            notes = "API endpoint that updates the question with this primary key"
    )
    public QuestionCreateUpdateRequest updateQuestion(
            @PathVariable("id") Long questionId,
            @RequestBody QuestionCreateUpdateRequest questionCreateUpdateRequest
    ) {
        return questionService.updateBy(questionId, questionCreateUpdateRequest);
    }

    @CrossOrigin
    @PutMapping("/{id}/generate-metrics")
    @ApiOperation(
            value = "Automatically generate metrics to the question",
            notes = "API endpoint that automatically generate metrics to the question"
    )
    public QuestionUpdateMetricsRequest generateMetrics(@PathVariable("id") Long questionId) {
        return questionService.generateMetrics(questionId);
    }

//    put:
//    """API endpoint to apply precooked metrics to the question"""

    @CrossOrigin
    @PutMapping("/{id}/assign-metrics")
    @ApiOperation(
            value = "Assigns metrics, chosen by the user, to the question",
            notes = "API endpoint that assign metrics, chosen by the user, to the question"
    )
    public QuestionUpdateMetricsRequest handPickMetrics(
            @PathVariable("id") Long questionId,
            @RequestBody QuestionUpdateMetricsRequest questionUpdateMetricsRequest
    ) {
        return questionService.updateMetricsBy(questionId, questionUpdateMetricsRequest);
    }

    @CrossOrigin
    @DeleteMapping("/{id}")
    @ApiOperation(
            value = "Deletes the question",
            notes = "API endpoint that deletes the question with this primary key"
    )
    public void deleteGoal(@PathVariable("id") Long questionId){
        questionService.deleteById(questionId);
    }

}
