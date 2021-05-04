package org.github.felipegutierrez.biddingsystem.auction.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * The bid response received from the bidders and that can be converted to the JSON format:
 * {{{
 * {
 * "id" : "id",
 * "bid": bid,
 * "content": "the string to deliver as a response"
 * }
 * }}}
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class BidResponse {
    private String id;
    private Integer bid;
    private String content;
}
