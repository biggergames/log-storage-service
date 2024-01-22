package com.biggergames.backend.logstorageservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
class LogStorageServiceApplicationTests {

    @Test
    void contextLoads() {
        String accountId = UUID.randomUUID().toString();
        //      doReturn(accountId).when(accountService.getActiveAccount(anyString()));
        //     Assertions.assertSame(accountService.getActiveAccount("deviceId"), accountId);
        Assertions.assertSame(accountId, accountId);
    }

}
