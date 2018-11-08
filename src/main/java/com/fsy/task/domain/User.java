package com.fsy.task.domain;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String username;

    private String password;

    @Override
    public String toString(){
        return username + "," + password;
    }
}
