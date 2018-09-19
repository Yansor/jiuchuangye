package com.fsy.task.dto.ResultDTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionDTO {
    @JsonProperty(value = "lid")
    private String lid; //135

    private String lCozId;//12

    private String strCode;//1.1

    private String strName;//认识职业发展与规划

    private String lParentId;//134

    private String strType;//section

    private String strComboName;//1.1 认识职业发展与规划

    private String strTimeLength;//10:49

    private int nTimeLength;//10

    private int nViewTimeLength;//0

    private String lVideoId;//23

    private String lCoursewareId;//46


}
