package com.innometrics.gqm_api.service;

import com.innometrics.gqm_api.dto.*;
import com.innometrics.gqm_api.exception.ForbiddenException;
import com.innometrics.gqm_api.exception.NotFoundException;
import com.innometrics.gqm_api.metrics_generation.MetricsGenerator;
import com.innometrics.gqm_api.model.Question;
import com.innometrics.gqm_api.repositories.GoalRepository;
import com.innometrics.gqm_api.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final MetricService metricService;
    private final GoalRepository goalRepository;
    private final QuestionRepository questionRepository;
    private final MetricsGenerator metricsGenerator;

    @Transactional
    public Question save(Question question) {
        return questionRepository.save(question);
    }

    @Transactional
    public QuestionRetrieveResponse getDtoById(Long questionId) {
        return QuestionRetrieveResponse.buildFrom(
                getQuestionById(
                        questionId,
                        new NotFoundException("Question with id  " + questionId + "not found")
                )
        );
    }

    @Transactional
    public Question getQuestionById(Long questionId, RuntimeException exception) {
        return questionRepository.findById(questionId).orElseThrow(
                () -> exception
        );
    }

    public List<QuestionRetrieveResponse> getAllDtos() {
        return getAll().stream()
                .map(QuestionRetrieveResponse::buildFrom)
                .collect(Collectors.toList());
    }

    public List<Question> getAll() {
        return questionRepository.findAll();
    }

    @Transactional
    public QuestionCreateUpdateRequest createDtoFrom(QuestionCreateUpdateRequest questionCreateUpdateRequest) {
        val resultQuestion = Question.buildFrom(questionCreateUpdateRequest);
        goalRepository.findById(questionCreateUpdateRequest.getGoalId())
                      .ifPresent(resultQuestion::setGoal);
        return QuestionCreateUpdateRequest.buildFrom(save(resultQuestion));
    }

    public QuestionCreateUpdateRequest updateBy(
            Long questionId,
            QuestionCreateUpdateRequest questionCreateUpdateRequest) {
        val updatable = getQuestionById(
                questionId,
                new ForbiddenException("Question with id  " + questionId + "does not exist")
        );
        updateFields(updatable, Question.buildFrom(questionCreateUpdateRequest));
        return QuestionCreateUpdateRequest.buildFrom(save(updatable));
    }

    private void updateFields(Question updatable, Question updater) {
        updatable.setContent(updater.getContent());
    }

    public QuestionUpdateMetricsRequest generateMetrics(Long questionId) {
        val updatable = getQuestionById(
                questionId,
                new NotFoundException("Question with id  " + questionId + "not found")
        );
        val metricsIds = metricsGenerator.generateMetrics(
                getQuestionById(
                        questionId,
                        new ForbiddenException("Question with id  " + questionId + "not found")
                ).getContent(),
                getAllQuestionsExceptGiven(questionId)
        );
        val newMetrics = metricService.getAllByIds(metricsIds);
        updatable.setMetrics(new HashSet<>(newMetrics));
        return QuestionUpdateMetricsRequest.buildFrom(save(updatable));
    }

    public List<QuestionGenerateMetricsRequest> getAllQuestionsExceptGiven(Long questionId) {
        return questionRepository.findAllByIdIsNot(questionId).stream()
                .map(QuestionGenerateMetricsRequest::buildFrom)
                .collect(Collectors.toList());
    }

    public QuestionUpdateMetricsRequest updateMetricsBy(
            Long questionId,
            QuestionUpdateMetricsRequest questionUpdateMetricsRequest
    ) {
        val updatable = getQuestionById(
                questionId,
                new NotFoundException("Question with id  " + questionId + "not found")
        );
        val newMetrics = metricService.getAllByIds(questionUpdateMetricsRequest.getMetricIds());
        updatable.setMetrics(new HashSet<>(newMetrics));
        return QuestionUpdateMetricsRequest.buildFrom(save(updatable));
    }

    public void deleteById(Long questionId) {
        getQuestionById(questionId, new ForbiddenException("Question with id  " + questionId + "does not exist"));
        questionRepository.deleteById(questionId);
    }

}
