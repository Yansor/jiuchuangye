package com.fsy.task.dto;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDto {
    private String nExaminType;

    private String lId;

    private String strExaminName;

    private Integer nTimeLength;

    private String nCourseRate;

    private Integer nPassScore;

    private Integer nScore;

    private String strPlanName;

    private String dtExaminStartTime;

    private String dtExaminEndTime;

    private String lExaminId;

    private String nStart;

    private Integer nScore0;

    private Integer nScore1;

    private Integer nExaminState;

    private Integer nScoreState;




}
