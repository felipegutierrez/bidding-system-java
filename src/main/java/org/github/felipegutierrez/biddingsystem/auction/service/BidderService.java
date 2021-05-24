package org.github.felipegutierrez.biddingsystem.auction.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.github.felipegutierrez.biddingsystem.auction.domain.BidRequest;
import org.github.felipegutierrez.biddingsystem.auction.domain.BidResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Service
public class BidderService {

    /**
     * List of bidders with WebClient
     */
    private final Set<WebClient> biddersWebClient = new HashSet<WebClient>();
    private final MeterRegistry meterRegistry;
    /**
     * List of bidders from args
     */
    @Getter
    @Value("${bidders:http://localhost:8081, http://localhost:8082, http://localhost:8083}")
    private List<String> bidders;
    private Counter bidderRequestCallsCounter;
    private Counter bidderRequestCallsFailCounter;

    public BidderService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initBidderCallCounter();
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

    private void initBidderCallCounter() {
        bidderRequestCallsCounter = this.meterRegistry.counter("bidder.calls.success", "type", "bidder");
        bidderRequestCallsFailCounter = this.meterRegistry.counter("bidder.calls.fail", "type", "bidder");
    }

    /**
     * request bids from bidders:
     * curl -i -X POST "http://localhost:8081" -H "Content-Type: application/json" -d "{\"id\":2,\"attributes\":{\"c\":\"5\",\"b\":\"2\"}}"
     *
     * @param bidRequestMono
     * @return
     */
    private Stream<Flux<BidResponse>> bidResponseStreamMono(Mono<BidRequest> bidRequestMono) {
        return biddersWebClient.stream()
                .map(bidderWebClient -> {
                    bidderRequestCallsCounter.increment();
                    return bidderWebClient.post()
                            .uri("/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(bidRequestMono, BidRequest.class)
                            .retrieve()
                            .bodyToFlux(BidResponse.class)
                            .timeout(Duration.ofMillis(3000))
                            .doOnError(throwable -> bidderRequestCallsFailCounter.increment())
                            .onErrorReturn(new BidResponse("", 0, "$price$"))
                            .log("BidResponse: ");
                });
    }

    /**
     * request bids from bidders:
     * curl -i -X POST "http://localhost:8081" -H "Content-Type: application/json" -d "{\"id\":2,\"attributes\":{\"c\":\"5\",\"b\":\"2\"}}"
     *
     * @param bidRequest
     * @return
     */
    public Stream<Flux<BidResponse>> bidResponseStream(BidRequest bidRequest) {
        return bidResponseStreamMono(Mono.just(bidRequest));
    }
}
