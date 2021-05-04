package org.github.felipegutierrez.biddingsystem.auction.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class BidRequest {
    private Integer id;
    private Map<String, String> attributes;
}
