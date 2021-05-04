package org.github.felipegutierrez.biddingsystem.auction.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.github.felipegutierrez.biddingsystem.auction.message.BidRequest;
import org.github.felipegutierrez.biddingsystem.auction.message.BidResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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

    private final Set<WebClient> biddersWebClient = new HashSet<WebClient>();
    @Getter
    @Value("${bidders:http://localhost:8081, http://localhost:8082, http://localhost:8083}")
    private List<String> bidders;

    /**
     * create a request like:
     * curl -i -X POST "http://localhost:8081" -H "Content-Type: application/json" -d "{\"id\":2,\"attributes\":{\"c\":\"5\",\"b\":\"2\"}}"
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
                            .log("BidResponse ");
                });
    }

    @Bean
    public void createBiddersWebClient() {
        bidders.forEach(bidder -> {
            biddersWebClient.add(WebClient.create(bidder));
            log.info("added bidder: " + bidder);
        });
    }
}
