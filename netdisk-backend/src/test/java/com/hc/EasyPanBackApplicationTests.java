package com.hc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.TimeZone;

@SpringBootTest
class EasyPanBackApplicationTests {

    @Test
    void contextLoads() {
        TimeZone timeZone = TimeZone.getDefault();
        if (timeZone.getID().equals("UTC")){
            System.out.println(11);
        }else {
            System.out.println(22);
        }
    }

}
