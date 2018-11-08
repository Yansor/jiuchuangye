package com.fsy.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {
    @JsonProperty(value = "lid")  //2400006361
    private String lId ;

    @JsonProperty(value = "lplanid")
    private String lPlanId ;  //2400000005

    private String strPlanName; //大学生职业发展与就业指导2018~2019-1

    private int nPlanState;  //0

    private String strStartDate ; //2018-09-03

    private String strEndDate ; //2018-12-30

    private int nSumScore; //0

    private String strCourseData; //[{"lId":"12","chapterList":[{"lChapterId":"134","nChapterTimeLength":"55"},{"lChapterId":"392","nChapterTimeLength":"30"},{"lChapterId":"395","nChapterTimeLength":"35"}],"strTypeName":"职业发展>建立生涯意识","strName":"职业发展1——意识建立篇","lLecturerId":"19","strLecturerName":"刘锐","nCourseTime":"120","nCourseHour":"4","strPicture":"http://wnsse-web1.oss-cn-beijing.aliyuncs.com/ceol-prod/course/1515646588000mkQ4BDmXXN.png"},{"lId":"13","chapterList":[{"lChapterId":"155","nChapterTimeLength":"127"},{"lChapterId":"324","nChapterTimeLength":"75"},{"lChapterId":"332","nChapterTimeLength":"117"},{"lChapterId":"329","nChapterTimeLength":"39"}],"strTypeName":"职业发展>职业发展规划","strName":"职业发展2——职业规划篇","lLecturerId":"25","strLecturerName":"金蕾莅","nCourseTime":"358","nCourseHour":"10","strPicture":"http://wnsse-web1.oss-cn-beijing.aliyuncs.com/ceol-prod/course/1515646517000nD4nE5nsZ5.png"},{"lId":"14","chapterList":[{"lChapterId":"177","nChapterTimeLength":"44"},{"lChapterId":"273","nChapterTimeLength":"22"},{"lChapterId":"275","nChapterTimeLength":"71"}],"strTypeName":"职业发展>提高就业能力","strName":"职业发展3——能力提升篇","lLecturerId":"13","strLecturerName":"王建鹏","nCourseTime":"159","nCourseHour":"5","strPicture":"http://wnsse-web1.oss-cn-beijing.aliyuncs.com/ceol-prod/course/1515646478000tePkAETTwK.png"},{"lId":"15","chapterList":[{"lChapterId":"138","nChapterTimeLength":"64"},{"lChapterId":"150","nChapterTimeLength":"43"},{"lChapterId":"143","nChapterTimeLength":"147"},{"lChapterId":"174","nChapterTimeLength":"137"},{"lChapterId":"284","nChapterTimeLength":"33"}],"strTypeName":"职业发展>求职过程指导","strName":"职业发展4——求职指导篇","lLecturerId":"7","strLecturerName":"韩速","nCourseTime":"428","nCourseHour":"8","strPicture":"http://wnsse-web1.oss-cn-beijing.aliyuncs.com/ceol-prod/course/1515646437000kXjbcNipBC.png"},{"lId":"35","chapterList":[{"lChapterId":"211","nChapterTimeLength":"43"},{"lChapterId":"214","nChapterTimeLength":"29"},{"lChapterId":"336","nChapterTimeLength":"31"}],"strTypeName":"职业发展>职场角色转换","strName":"职业发展5——职业适应篇","lLecturerId":"11","strLecturerName":"李凤","nCourseTime":"103","nCourseHour":"5","strPicture":"http://wnsse-web1.oss-cn-beijing.aliyuncs.com/ceol-prod/course/151564583800044EZY5idc2.png"}]

    private String strEvaluationData;

    private String strState; //进行中

    private boolean bScoreState;//false

    private int ninvalid; //0

    private String lPrePlanId; //0

    private int nCourseCount; //5

    private int nTotalStudyTime; //0

    private int nTotalCourseTime; //1142

    private int nCoursewareCount; //55

    private String firstCourseName; //职业发展1——意识建立篇

}
