package com.fsy.task.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonDto {

    @JsonProperty(value = "return")
    private CommonReturnDto returnAlias;

    private CommonResponse response;
}
