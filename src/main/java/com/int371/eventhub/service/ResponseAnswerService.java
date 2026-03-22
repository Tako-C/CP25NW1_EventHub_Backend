package com.int371.eventhub.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.ResponseAnswerSearchDto;
import com.int371.eventhub.entity.QuestionType;
import com.int371.eventhub.entity.ResponseAnswer;
import com.int371.eventhub.entity.SuggestionsAnalysis;
import com.int371.eventhub.repository.SuggestionsAnalysisRepository;

@Service
public class ResponseAnswerService {

    @Autowired
    private SuggestionsAnalysisRepository suggestionsAnalysisRepository;

    public List<ResponseAnswerSearchDto> searchAnswersByKeyword(String keyword) {
        List<SuggestionsAnalysis> suggestions = suggestionsAnalysisRepository
                .findByKeywordContainingIgnoreCaseAndResponseAnswerQuestionType(keyword, QuestionType.TEXT);

        return suggestions.stream().map(suggestion -> {
            ResponseAnswer answer = suggestion.getResponseAnswer();
            Integer eventId = null;
            if (answer.getMemberEvent() != null && answer.getMemberEvent().getEvent() != null) {
                eventId = answer.getMemberEvent().getEvent().getId();
            }

            return new ResponseAnswerSearchDto(
                    answer.getId(),
                    answer.getQuestion() != null ? answer.getQuestion().getId() : null,
                    answer.getQuestion() != null ? answer.getQuestion().getQuestion() : null,
                    answer.getAnswer(),
                    suggestion.getKeyword(),
                    suggestion.getSentiment(),
                    answer.getQuestionType() != null ? answer.getQuestionType().name() : null,
                    eventId);
        }).collect(Collectors.toList());
    }
}
