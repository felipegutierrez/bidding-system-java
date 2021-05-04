package org.github.felipegutierrez.biddingsystem.auction.handler;

import lombok.extern.slf4j.Slf4j;
import org.github.felipegutierrez.biddingsystem.auction.domain.BidResponse;
import org.github.felipegutierrez.biddingsystem.auction.service.BidderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuctionHandlerFunc {

    @Autowired
    private final BidderService bidderService;

    public AuctionHandlerFunc(BidderService bidderService) {
        this.bidderService = bidderService;
    }

    /**
     * Receive GET requests from http://localhost:8080
     * @param serverRequest
     * @return
     */
    public Mono<ServerResponse> bidRequest(ServerRequest serverRequest) {
        var adId = serverRequest.pathVariable("id");
        var attributes = serverRequest.queryParams();
        log.info("received bid request with adID: {} attributes: {}", adId, attributes);

        // send POST request to all bidders and collect responses
        var bidResponseFlux = Flux
                .fromStream(bidderService.bidResponseStream(adId, attributes.toSingleValueMap()))
                // .concatMap(this::gatherResponses);
                .flatMap(this::gatherResponses);

        // process the winner bid
        var bidWinnerMono = bidResponseFlux
                .reduce((bidResp1, bidResp2) -> {
                    if (bidResp1.getBid() > bidResp2.getBid()) return bidResp1;
                    else return bidResp2;
                    // problem related to: https://github.com/felipegutierrez/bidding-system-java/issues/1
//                    else if (bidResp1.getBid() < bidResp2.getBid()) return bidResp2;
//                    else {
//                        if (bidResp1.getContent().contains("a")) return bidResp1;
//                        else if (bidResp2.getContent().contains("a")) return bidResp2;
//                        else if (bidResp1.getContent().contains("b")) return bidResp1;
//                        else if (bidResp2.getContent().contains("b")) return bidResp2;
//                        else if (bidResp1.getContent().contains("c")) return bidResp1;
//                        else return bidResp2;
//                    }
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
