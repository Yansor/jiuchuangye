package com.fsy.task.util;

import com.fsy.task.domain.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserImportUtil {

    public static List<User>  getImportUserList(String filePath) throws IOException {
        List<User> users = new ArrayList<User>();
        BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
        String line = null;
        while((line = br.readLine()) != null){
            users.add(User.builder()
                    .username(line.split(",")[0])
                    .password(line.split(",")[1])
                    .build());
        }
        return users;
    }
}
