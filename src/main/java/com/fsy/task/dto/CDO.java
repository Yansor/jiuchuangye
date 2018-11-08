package com.fsy.task.dto;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CDO {

    private String nCode;

    private String strText;

    private String strInfo;

    private String lId;

    private String strLoginId;

    private String strHash;

    private String strName;

    private String strNickName;

    private String nVerifyStage;

    private String dtLastLoginTime;

    private String strHeadURL;

    private String bLocked;

    private String bIsRegUser;

    private String strEmail;

    private String lSchoolId;

    private String nRecordCount;

    private String arrLoginUrl;

    private String nType;
}
