package org.github.felipegutierrez.biddingsystem.auction.router;

import org.github.felipegutierrez.biddingsystem.auction.handler.AuctionServerHandlerFunc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

import static org.github.felipegutierrez.biddingsystem.auction.util.BidConstants.BID_REQUEST_ENDPOINT_V1;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class AuctionServerRouterFuncConfig {

    @Bean
    public RouterFunction route(AuctionServerHandlerFunc auctionServerHandlerFunc) {
        return RouterFunctions
                .route(GET(BID_REQUEST_ENDPOINT_V1).and(accept(MediaType.APPLICATION_JSON)),
                        auctionServerHandlerFunc::bidRequest);
    }
}
