package com.common.thirdpartyservice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SMSUtilTest {

    @Test
    void sendSMS() {
        SMSUtil.sendSMS("18096295136");
    }
}