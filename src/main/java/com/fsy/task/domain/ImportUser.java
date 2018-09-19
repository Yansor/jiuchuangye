package com.fsy.task.domain;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportUser {

    private String username;

    private String password;

    //private String schoolId ; 不导入 程序直接自动获取

    //private String userId;   不导入 程序直接自动获取

    private String schoolToken ;

    private String nickName;

    private String loginDomain;


}
