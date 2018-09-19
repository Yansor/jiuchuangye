package com.fsy.task;

import com.fsy.task.selenium.SeleniumUtil;
import org.junit.jupiter.api.Test;

public class SeleniumTests {
    @Test
    public void tests(){

        //sdnu201715030108 ncy1224
        new SeleniumUtil().getUserInfo("sdnu201715030108" , "ncy1224");
    }
}
