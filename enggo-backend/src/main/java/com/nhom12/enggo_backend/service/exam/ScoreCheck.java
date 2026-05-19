package com.nhom12.enggo_backend.service.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Score;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ScoreCheck {
    private BigDecimal score;
    private boolean correct;

    public static ScoreCheck correct (){
        return new ScoreCheck(BigDecimal.ONE, true);
    }

    public static ScoreCheck notCorrect (){
        return new ScoreCheck(BigDecimal.ZERO, false);
    }

    public static ScoreCheck partial (BigDecimal score, boolean isCorrect){
        return new ScoreCheck(score, isCorrect);
    }
}
