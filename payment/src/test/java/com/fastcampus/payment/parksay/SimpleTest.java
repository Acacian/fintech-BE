package com.fastcampus.payment.parksay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleTest {

    @Test
    public void ttest() {
        String target = "0123-4567-8910-1112";
        int lastIdx = target.lastIndexOf("-");
        String result = target.substring(lastIdx + 1);
        assertEquals("1112", result);
    }
}
