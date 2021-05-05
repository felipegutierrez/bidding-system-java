package org.github.felipegutierrez.biddingsystem.auction.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.github.felipegutierrez.biddingsystem.auction.domain.BidRequest;
import org.github.felipegutierrez.biddingsystem.auction.domain.BidResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Service
public class BidderService {

    /**
     * List of bidders with WebClient
     */
    private final Set<WebClient> biddersWebClient = new HashSet<WebClient>();

    /**
     * List of bidders from args
     */
    @Getter
    @Value("${bidders:http://localhost:8081, http://localhost:8082, http://localhost:8083}")
    private List<String> bidders;

    /**
     * request bids from bidders:
     * curl -i -X POST "http://localhost:8081" -H "Content-Type: application/json" -d "{\"id\":2,\"attributes\":{\"c\":\"5\",\"b\":\"2\"}}"
     *
     * @param adId
     * @param attributes
     * @return
     */
    public Stream<Flux<BidResponse>> bidResponseStream(String adId, Map<String, String> attributes) {
        return biddersWebClient.stream()
                .map(bidderWebClient -> {
                    var bidRequest = new BidRequest(Integer.parseInt(adId), attributes);
                    return bidderWebClient.post()
                            .uri("/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(Mono.just(bidRequest), BidRequest.class)
                            .retrieve()
                            .bodyToFlux(BidResponse.class)
                            .onErrorReturn(new BidResponse(adId, 0, "$price$"))
                            .log("BidResponse: ");
                });
    }

    /**
     * create the list of bidders
     */
    @Bean
    public void createBiddersWebClient() {
        bidders.forEach(bidder -> {
            biddersWebClient.add(WebClient.create(bidder));
            log.info("added bidder: " + bidder);
        });
    }

    private Stream<Flux<BidResponse>> bidResponseStreamMono(Mono<BidRequest> bidRequestMono) {
        return biddersWebClient.stream()
                .map(bidderWebClient -> {
                    return bidderWebClient.post()
                            .uri("/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(bidRequestMono, BidRequest.class)
                            .retrieve()
                            .bodyToFlux(BidResponse.class)
                            .onErrorReturn(new BidResponse("", 0, "$price$"))
                            .log("BidResponse: ");
                });
    }

    public Stream<Flux<BidResponse>> bidResponseStream(BidRequest bidRequest) {
        return bidResponseStreamMono(Mono.just(bidRequest));
    }
}
