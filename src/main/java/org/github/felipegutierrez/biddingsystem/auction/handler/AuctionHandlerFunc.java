package org.github.felipegutierrez.biddingsystem.auction.handler;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.github.felipegutierrez.biddingsystem.auction.domain.BidRequest;
import org.github.felipegutierrez.biddingsystem.auction.domain.BidResponse;
import org.github.felipegutierrez.biddingsystem.auction.service.BidderService;
import org.github.felipegutierrez.biddingsystem.auction.util.GenericValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AuctionHandlerFunc {

    @Autowired
    private final BidderService bidderService;

    private final MeterRegistry meterRegistry;
    private Gauge auctionInProgressGauge;
    private List<Integer> auctionInProgress;

    public AuctionHandlerFunc(BidderService bidderService, MeterRegistry meterRegistry) {
        this.bidderService = bidderService;
        this.meterRegistry = meterRegistry;
        initAuctionGauge();
    }

    private void initAuctionGauge() {
        this.auctionInProgress = new ArrayList<>(4);
        this.auctionInProgressGauge = Gauge
                .builder("bidder.auction.inprogress", auctionInProgress, List::size)
                .tags("type", "bidder")
                .tags("type", "auction")
                .register(this.meterRegistry);
    }

    /**
     * Receive GET requests from http://localhost:8080
     *
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> bidRequest(ServerRequest serverRequest) {
        var adId = serverRequest.pathVariable("id");
        var attributes = serverRequest.queryParams();
        log.info("received bid request with adID: {} attributes: {}", adId, attributes);
        auctionInProgress.add(1);

        return Mono
                .just(Tuples.of(adId, attributes))
                .flatMap(tuple2 -> {
                    if (validate(tuple2)) {
                        log.info("request parameters valid: {}", tuple2);
                        return Mono.just(new BidRequest(Integer.parseInt(tuple2.getT1()), tuple2.getT2().toSingleValueMap()));
                    } else {
                        log.error("request parameters invalid: {}", tuple2);
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
                    }
                })
                .flatMap(bidRequest -> {
                    return Flux.fromStream(bidderService.bidResponseStream(bidRequest))
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST)))
                            .flatMap(this::gatherResponses)
                            .reduce((bidResp1, bidResp2) -> {
                                log.info("filtering the maximum bid: " + bidResp1 + " - " + bidResp2);
                                if (bidResp1.getBid() > bidResp2.getBid()) return bidResp1;
                                else if (bidResp1.getBid() < bidResp2.getBid()) return bidResp2;
                                else {
                                    log.warn("There is a tie of bidders: {} vs {}", bidResp1, bidResp2);
                                    return bidResp1;
                                }
                            });
                })
                .map(bid -> {
                    var price = bid.getContent().replace("$price$", bid.getBid().toString());
                    bid.setContent(price);
                    return bid;
                })
                .flatMap(winner -> {
                    log.info("The winner is: {}", winner);
                    auctionInProgress.remove(0);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(winner.getContent()));
                })
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(error -> ServerResponse.badRequest().build());
    }

    private boolean validate(Tuple2<String, MultiValueMap<String, String>> tuple2) {
        return GenericValidator.isInteger(tuple2.getT1());
    }

    private Flux<BidResponse> gatherResponses(Flux<BidResponse> bidResponseFlux) {
        try {
            return bidResponseFlux;
        } catch (Exception e) {
            log.warn("exception: ", e.getMessage());
            return Flux.empty();
        }
    }
}
