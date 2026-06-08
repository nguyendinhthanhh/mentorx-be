package com.mentorx.api.feature.course.entity;

import com.mentorx.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "quiz_answers", indexes = {
        @Index(name = "idx_quiz_answer_attempt", columnList = "attempt_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(name = "given_answer_json", nullable = false, columnDefinition = "TEXT")
    private String givenAnswerJson;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "points_earned", nullable = false, precision = 8, scale = 2)
    private BigDecimal pointsEarned;
}
