package org.github.felipegutierrez.biddingsystem.auction.handler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.github.felipegutierrez.biddingsystem.auction.message.BidRequest;
import org.github.felipegutierrez.biddingsystem.auction.message.BidResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class AuctionServerHandlerFunc {

    @Getter
    @Value("${bidders:http://localhost:8081, http://localhost:8082, http://localhost:8083}")
    private List<String> bidders;

    private Set<WebClient> biddersWebClient;

    public Mono<ServerResponse> bidRequest(ServerRequest serverRequest) {
        var adId = serverRequest.pathVariable("id");
        var attributes = serverRequest.queryParams();
        log.info("received bid request with adID: {} attributes: {}", adId, attributes);

        log.info("bidders");
        bidders.forEach(System.out::println);

        BidRequest bidRequest = new BidRequest(Integer.parseInt(adId), attributes.toSingleValueMap());

        /**
         * create a request like:
         * curl -i -X POST "http://localhost:8081" -H "Content-Type: application/json" -d "{\"id\":2,\"attributes\":{\"c\":\"5\",\"b\":\"2\"}}"
         */
        var bidResponseFlux = Flux.fromIterable(biddersWebClient)
                .flatMap(bidderWebClient -> {
                    return bidderWebClient.post()
                            .uri("/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(bidRequest), BidRequest.class)
                            .retrieve()
                            .bodyToFlux(BidResponse.class)
                            .timeout(Duration.ofMillis(5000))
                            .log("BidResponse: ");
                });

        BidResponse bidResponse = new BidResponse().setBid(1).setBid(750).setContent("a:750");

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(bidResponse.getContent()), String.class);
    }

    @Bean
    public void createBiddersWebClient() {
        bidders.forEach(bidder -> {
            biddersWebClient = Set.of(WebClient.create(bidder));
        });
    }
}
