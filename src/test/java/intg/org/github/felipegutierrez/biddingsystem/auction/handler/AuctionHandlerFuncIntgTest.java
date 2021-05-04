package org.github.felipegutierrez.biddingsystem.auction.handler;

import org.junit.jupiter.api.Disabled;
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
class AuctionHandlerFuncIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @Disabled("It is necessary to start the docker clients")
    void bidRequest_complete() {

        var bidRequest = BID_REQUEST_ENDPOINT_V1
                .replace("{id}", "2")
                .replace("{attributes}", "?c=5&b=2");

        webTestClient.get().uri(bidRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(String.class);

    }

    @Test
    @Disabled("It is necessary to start the docker clients")
    void bidRequest_noAttributes() {
        var bidRequest = BID_REQUEST_ENDPOINT_V1
                .replace("{id}", "234")
                .replace("{attributes}", "");

        webTestClient.get().uri(bidRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(String.class);
    }
}