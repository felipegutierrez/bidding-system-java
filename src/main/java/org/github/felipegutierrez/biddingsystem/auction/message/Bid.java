package org.github.felipegutierrez.biddingsystem.auction.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Accessors(chain = true)
public class Bid {
    private Integer id;
    private Map<String, String> attributes;
}
