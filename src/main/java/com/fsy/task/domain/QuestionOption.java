package com.fsy.task.domain;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOption {

    private String questionId;

    private String questionOptionCount;
}
