package org.github.felipegutierrez.biddingsystem.auction.handler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.github.felipegutierrez.biddingsystem.auction.util.BidConstants.BID_REQUEST_ENDPOINT_V1;

@DirtiesContext
@AutoConfigureWebTestClient
@SpringBootTest
class AuctionHandlerFuncUnitTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    AuctionHandlerFunc auctionServerHandlerFunc;

    @Test
    void bidRequest_noId() {
        var bidRequest = BID_REQUEST_ENDPOINT_V1
                .replace("{id}", "")
                .replace("{attributes}", "");

        webTestClient.get().uri(bidRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(String.class);
    }

    @Test
    void bidRequest_wrong() {
        var bidRequest = BID_REQUEST_ENDPOINT_V1
                .replace("{id}", "234")
                .replace("{attributes}", "c=5b=2");

        webTestClient.get().uri(bidRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}