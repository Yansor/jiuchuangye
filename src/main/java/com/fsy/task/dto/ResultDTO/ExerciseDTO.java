package com.fsy.task.dto.ResultDTO;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseDTO {
    private String lId; //12

    private String strPicture; //http://wnsse-web1.oss-cn-beijing.aliyuncs.com/ceol-prod/course/1515646588000mkQ4BDmXXN.png

    private String lLecturerId; // 19

    private String strName; //刘锐

    private String strPhoto; //http://wnsse-web1.oss-cn-beijing.aliyuncs.com/ceol-prod/lecturer/1516674565000yaEEZfQhXx.jpg

    private List<SectionDTO> cdoSectionList; //  size = 7

    private String strEndDate; //null

    private int nCourseSectionCount; //0

}
