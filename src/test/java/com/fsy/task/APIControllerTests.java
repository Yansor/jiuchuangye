package com.fsy.task;

import com.fsy.task.domain.enums.AnswerOption;
import org.htmlparser.util.ParserException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class APIControllerTests {
    @Test
    public void test(){
        new APIController("sdnu201715030108" , "ncy1224");
    }
}