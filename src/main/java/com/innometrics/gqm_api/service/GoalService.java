package com.innometrics.gqm_api.service;

import com.innometrics.gqm_api.dto.GoalBaseDto;
import com.innometrics.gqm_api.dto.GoalRetrieveResponse;
import com.innometrics.gqm_api.dto.GoalWithIdDto;
import com.innometrics.gqm_api.dto.QuestionsForGoalDto;
import com.innometrics.gqm_api.exception.ForbiddenException;
import com.innometrics.gqm_api.exception.NotFoundException;
import com.innometrics.gqm_api.model.Goal;
import com.innometrics.gqm_api.repositories.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;

    @Transactional
    public Goal save(Goal goal) {
        return goalRepository.save(goal);
    }

    @Transactional
    public GoalRetrieveResponse getDtoById(Long goalId) {
        return GoalRetrieveResponse.buildFrom(
                getGoalById(goalId, new NotFoundException("Goal not found by id " + goalId))
        );
    }

    @Transactional
    public List<GoalRetrieveResponse> getDtosByUser(String email) {
      return getAllByUserEmail(email).stream().map(GoalRetrieveResponse::buildFrom).collect(Collectors.toList());
    }

    @Transactional
    public Goal getGoalById(Long goalId, RuntimeException exception) {
        return goalRepository.findById(goalId).orElseThrow(
                () -> exception
        );
    }

    public List<GoalRetrieveResponse> getAllDtos() {
        return getAllGoals().stream()
                .map(GoalRetrieveResponse::buildFrom)
                .collect(Collectors.toList());
    }

  public List<Goal> getAllByUserEmail(String email) {
    return goalRepository.findByUserEmail(email);
  }

    public List<Goal> getAllGoals() {
        return goalRepository.findAll();
    }

    @Transactional
    public QuestionsForGoalDto getQuestionsByGoalId(Long goalId) {
        return QuestionsForGoalDto.buildFrom(
                getGoalById(goalId, new NotFoundException("Goal not found by id " + goalId))
        );
    }

    public GoalWithIdDto createDtoFrom(GoalBaseDto goal) {
        val resultGoal = Goal.buildFrom(goal);
        return GoalWithIdDto.buildFrom(save(resultGoal));
    }

    @Transactional
    public GoalBaseDto updateBy(Long goalId, GoalBaseDto goalBaseDto) {
        val updatable = getGoalById(goalId, new NotFoundException("Goal not found by id " + goalId));
        updateFields(updatable, Goal.buildFrom(goalBaseDto));
        return GoalBaseDto.buildFrom(save(updatable));
    }

    private void updateFields(Goal updatable, Goal updater) {
        updatable.setContent(updater.getContent());
        updatable.setUserEmail(updater.getUserEmail());
    }

    public void deleteById(Long goalId) {
        getGoalById(goalId, new ForbiddenException("Goal with id  " + goalId + "does not exist"));
        goalRepository.deleteById(goalId);
    }

}
