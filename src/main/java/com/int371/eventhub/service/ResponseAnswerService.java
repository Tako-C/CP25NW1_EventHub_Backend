package com.int371.eventhub.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.ResponseAnswerSearchDto;
import com.int371.eventhub.entity.QuestionType;
import com.int371.eventhub.entity.ResponseAnswer;
import com.int371.eventhub.repository.ResponseAnswerRepository;

@Service
public class ResponseAnswerService {

    @Autowired
    private ResponseAnswerRepository responseAnswerRepository;

    public List<ResponseAnswerSearchDto> searchAnswersByKeyword(String keyword) {
        List<ResponseAnswer> answers = responseAnswerRepository
                .findByKeywordContainingIgnoreCaseAndQuestionType(keyword, QuestionType.TEXT);

        return answers.stream().map(answer -> {
            Integer eventId = null;
            if (answer.getMemberEvent() != null && answer.getMemberEvent().getEvent() != null) {
                eventId = answer.getMemberEvent().getEvent().getId();
            }

            return new ResponseAnswerSearchDto(
                    answer.getId(),
                    answer.getQuestion() != null ? answer.getQuestion().getId() : null,
                    answer.getQuestion() != null ? answer.getQuestion().getQuestion() : null,
                    answer.getAnswer(),
                    answer.getKeyword(),
                    answer.getSentiment(),
                    answer.getQuestionType() != null ? answer.getQuestionType().name() : null,
                    eventId);
        }).collect(Collectors.toList());
    }
}
