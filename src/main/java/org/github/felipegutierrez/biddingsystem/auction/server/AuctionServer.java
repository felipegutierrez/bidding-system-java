package org.github.felipegutierrez.biddingsystem.auction.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuctionServer {
    private List<String> bidders;
}
