package org.github.felipegutierrez.biddingsystem.auction.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@SpringBootTest
class BidderServiceUnitTest {

    @Autowired
    BidderService bidderService;

    @Test
    void getBidders() {
        Assertions.assertNotNull(bidderService.getBidders());
        Assertions.assertEquals(3, bidderService.getBidders().size());
        // http://localhost:8081, http://localhost:8082, http://localhost:8083
    }
}