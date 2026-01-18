package com.int371.eventhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "QUESTIONS")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SURVEY_ID", nullable = false)
    private Survey survey;

    @Column(name = "QUESTION", nullable = false)
    private String question;

    @Column(name = "ANSWER_1")
    private String answer1;

    @Column(name = "ANSWER_2")
    private String answer2;

    @Column(name = "ANSWER_3")
    private String answer3;

    @Column(name = "ANSWER_4")
    private String answer4;

    @Column(name = "ANSWER_5")
    private String answer5;

    @Enumerated(EnumType.STRING)
    @Column(name = "QUESTION_TYPE")
    private QuestionType questionType;
}