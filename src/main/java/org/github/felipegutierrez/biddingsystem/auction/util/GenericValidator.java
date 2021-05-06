package org.github.felipegutierrez.biddingsystem.auction.util;

import org.apache.logging.log4j.util.Strings;

public class GenericValidator {

    public static boolean isInteger(String value) {
        if (Strings.isEmpty(value) || Strings.isBlank(value)) {
            return false;
        } else {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return false;
            } catch (NullPointerException e) {
                return false;
            }
        }
        return true;
    }
}
