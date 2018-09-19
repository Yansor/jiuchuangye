package com.fsy.task;

import com.fsy.task.util.MD5Util;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

public class MD5UtilTests {
    @Test
    public void generate(){
        //32 123456 => e10adc3949ba59abbe56e057f20f883e
        String hexString = MD5Util.MD5Encode("123456" , Charset.defaultCharset().toString());
        System.out.println(hexString);
    }
}
