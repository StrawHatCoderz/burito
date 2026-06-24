package com.burito.identity.controller.views;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request model to update user delivery address")
public record UpdateAddressRequest(
    @Schema(description = "Street address", example = "123 Main St")
    String street,

    @Schema(description = "City", example = "Bengaluru")
    String city,

    @Schema(description = "State", example = "Karnataka")
    String state,

    @Schema(description = "Zip code", example = "560001")
    String zipcode,

    @Schema(description = "Country", example = "India")
    String country
) {
}
