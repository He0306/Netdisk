package com.hc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.TimeZone;

@SpringBootTest
class EasyPanBackApplicationTests {

    @Test
    void contextLoads() {
        String userid = "1667464398215524354_l3xIrn4QVy,1722255333935493122_XghNOI8kw0";
        String[] split = userid.split(",");
        for (String s : split) {
            String[] split1 = s.split("_");
            System.out.println(s);
            for (String s1 : split1) {
                System.out.println(s1);
            }

        }
    }

}
