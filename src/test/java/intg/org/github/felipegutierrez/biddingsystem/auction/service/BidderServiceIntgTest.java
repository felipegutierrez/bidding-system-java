package org.github.felipegutierrez.biddingsystem.auction.service;

import org.github.felipegutierrez.biddingsystem.auction.message.BidResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class BidderServiceIntgTest {

    @Mock
    BidderService bidderService;

    @Test
    void bidResponseStream() {

        Mockito.when(bidderService.bidResponseStream(anyString(), anyMap()))
                .thenReturn(Stream.of(Flux.just(new BidResponse("1", 2500, "c:2500"))));

        var response = bidderService.bidResponseStream("2",
                Map.of(
                        "c", "5",
                        "b", "2"
                )
        );
    }
}