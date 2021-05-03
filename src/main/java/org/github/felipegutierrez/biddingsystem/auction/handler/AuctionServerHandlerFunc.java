package org.github.felipegutierrez.biddingsystem.auction.handler;

import lombok.extern.slf4j.Slf4j;
import org.github.felipegutierrez.biddingsystem.auction.message.BidResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuctionServerHandlerFunc {

    public Mono<ServerResponse> bidRequest(ServerRequest serverRequest) {
        var id = serverRequest.pathVariable("id");
        var attributes = serverRequest.queryParams();
        log.info("received bid request with ID: {} attributes: {}", id, attributes);

        BidResponse bidResponse = new BidResponse().setBid(1).setBid(750).setContent("a:750");

        /*
        Mono<Integer> bidMono = serverRequest.bodyToMono(Integer.class)
                .map(idValue -> {
                    System.out.println("bidMono: " + idValue);
                    return Mono.just(idValue).block();
                });
        // log.info("bidMono: {}", bidMono.block());
         */

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(bidResponse.getContent()), String.class);
    }
}
