package org.github.felipegutierrez.biddingsystem.auction.service;

import org.github.felipegutierrez.biddingsystem.auction.domain.BidRequest;
import org.github.felipegutierrez.biddingsystem.auction.domain.BidResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class BidderServiceIntgTest {

    @Mock
    BidderService bidderService;

    @Test
    void bidResponseStream() {

        Mockito.when(bidderService.bidResponseStream(any()))
                .thenReturn(Stream.of(Flux.just(new BidResponse("1", 2500, "c:2500"))));

        var bidResponseFlux = Flux.fromStream(bidderService.bidResponseStream(new BidRequest()))
                .flatMap(this::gatherResponses);

        StepVerifier.create(bidResponseFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void bidResponseStreamSpecificBidResponse() {

        Mockito.when(bidderService.bidResponseStream(any()))
                .thenReturn(Stream.of(Flux.just(new BidResponse("1", 2500, "c:2500"))));

        var bidResponseFlux = Flux.fromStream(bidderService.bidResponseStream(new BidRequest()))
                .flatMap(this::gatherResponses);

        StepVerifier.create(bidResponseFlux)
                .expectNext(new BidResponse("1", 2500, "c:2500"))
                .verifyComplete();
    }

    private Flux<BidResponse> gatherResponses(Flux<BidResponse> bidResponseFlux) {
        try {
            return bidResponseFlux;
        } catch (Exception e) {
            return Flux.empty();
        }
    }
}
