package org.github.felipegutierrez.biddingsystem.auction.validator;

import org.github.felipegutierrez.biddingsystem.auction.domain.BidRequest;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class BidRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return BidRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "id", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "attributes", "field.required");
        BidRequest bidRequest = (BidRequest) target;
        if (bidRequest.getId() == null) {
            errors.rejectValue(
                    "id",
                    "field.min.length",
                    new Object[] { Integer.valueOf(6) },
                    "The code must be at least [6] characters in length.");
        }
    }
}
