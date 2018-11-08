package com.fsy.task.dto;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamListResponseDto {
    private List<ExamDto> cdosUserExaminList;

    private String nRecordCount;

    private Integer nPageCount;
}
