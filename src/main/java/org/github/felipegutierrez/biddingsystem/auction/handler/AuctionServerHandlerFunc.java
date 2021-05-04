package org.github.felipegutierrez.biddingsystem.auction.handler;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.github.felipegutierrez.biddingsystem.auction.message.BidRequest;
import org.github.felipegutierrez.biddingsystem.auction.message.BidResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Component
public class AuctionServerHandlerFunc {

    private final Set<WebClient> biddersWebClient = new HashSet<WebClient>();
    @Getter
    @Value("${bidders:http://localhost:8081, http://localhost:8082, http://localhost:8083}")
    private List<String> bidders;

    public Mono<ServerResponse> bidRequest(ServerRequest serverRequest) {
        var adId = serverRequest.pathVariable("id");
        var attributes = serverRequest.queryParams();
        log.info("received bid request with adID: {} attributes: {}", adId, attributes);

        // send POST request to all bidders and collect responses
        var bidResponseFlux = Flux
                .fromStream(bidResponseStream(adId, attributes))
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(this::gatherResponses);

        // process the winner bid
        var bidWinnerMono = bidResponseFlux
                .reduce((bidResp1, bidResp2) -> {
                    if (bidResp1.getBid() > bidResp2.getBid()) return bidResp1;
                    else return bidResp2;
                })
                .map(bid -> {
                    var price = bid.getContent().replace("$price$", bid.getBid().toString());
                    bid.setContent(price);
                    return bid;
                });

        // return the response with the winner bid in JSON format
        return bidWinnerMono
                .flatMap(winner -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(winner.getContent())))
                .switchIfEmpty(ServerResponse.notFound().build());

        //        BidResponse bidResponse = new BidResponse().setBid(1).setBid(750).setContent("a:750");
        //        return ServerResponse
        //                .ok()
        //                .contentType(MediaType.APPLICATION_JSON)
        //                .body(Mono.just(bidResponse.getContent()), String.class);
    }

    /**
     * create a request like:
     * curl -i -X POST "http://localhost:8081" -H "Content-Type: application/json" -d "{\"id\":2,\"attributes\":{\"c\":\"5\",\"b\":\"2\"}}"
     */
    private Stream<Flux<BidResponse>> bidResponseStream(String adId, MultiValueMap<String, String> attributes) {
        return biddersWebClient.stream()
                .map(bidderWebClient -> {
                    var bidRequest = new BidRequest(Integer.parseInt(adId), attributes.toSingleValueMap());
                    return bidderWebClient.post()
                            .uri("/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(bidRequest), BidRequest.class)
                            .retrieve()
                            .bodyToFlux(BidResponse.class)
                            // .timeout(Duration.ofMillis(5000))
                            .log("BidResponse ");
                });
    }

    private Flux<BidResponse> gatherResponses(Flux<BidResponse> bidResponseFlux) {
        try {
            return bidResponseFlux;
        } catch (Exception e) {
            log.warn("exception: ", e.getMessage());
            return Flux.empty();
        }
    }

    @Bean
    public void createBiddersWebClient() {
        bidders.forEach(bidder -> {
            biddersWebClient.add(WebClient.create(bidder));
            log.info("added bidder: " + bidder);
        });
    }
}
