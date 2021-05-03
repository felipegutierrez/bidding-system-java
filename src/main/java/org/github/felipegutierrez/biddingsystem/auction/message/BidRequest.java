package org.github.felipegutierrez.biddingsystem.auction.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class BidRequest {
    private String requestId;
    private Bid bid;
}
