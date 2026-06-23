package com.burito.catalog.controller;
import com.burito.catalog.controller.views.*;
import com.burito.identity.controller.views.*;
import com.burito.ordering.controller.views.*;
import com.burito.core.controller.views.*;
import com.burito.core.controller.views.APIResponse;
import com.burito.core.controller.views.ApiError;

import com.burito.core.controller.views.APIResponse;
import com.burito.core.controller.views.ApiError;
import com.burito.catalog.domain.MenuItem;
import com.burito.catalog.domain.Restaurant;
import com.burito.catalog.enums.CuisineType;
import com.burito.core.exceptions.APIException;
import com.burito.catalog.exceptions.InvalidCuisineTypeException;
import com.burito.catalog.service.MenuService;
import com.burito.catalog.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Restaurants", description = "Browse the restaurant catalog — no authentication required")
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
  private final RestaurantService restaurantService;
  private final MenuService menuService;

  public RestaurantController(RestaurantService restaurantService, MenuService menuService) {
    this.restaurantService = restaurantService;
    this.menuService = menuService;
  }

  @Operation(summary = "List restaurants, optionally filtered by name and/or cuisine type")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Returns the filtered restaurant list"),
      @ApiResponse(responseCode = "400", description = "cuisine value is not a valid CuisineType — errorCode: INVALID_CUISINE_TYPE",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping({"", "/"})
  public ResponseEntity<APIResponse<List<Restaurant>>> serveAllRestaurant(
      @Parameter(description = "Case-insensitive name filter") @RequestParam(required = false) String search,
      @Parameter(description = "Cuisine type filter — must match a CuisineType enum value") @RequestParam(required = false) String cuisine)
      throws APIException {
    CuisineType cuisineType = null;
    if (cuisine != null) {
      try {
        cuisineType = CuisineType.valueOf(cuisine.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new InvalidCuisineTypeException(cuisine);
      }
    }
    List<Restaurant> restaurantList = restaurantService.search(search, cuisineType);
    return ResponseEntity.ok(APIResponse.success(restaurantList));
  }

  @Operation(summary = "Get a single restaurant by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Restaurant found"),
      @ApiResponse(responseCode = "400", description = "restaurantId is not a valid UUID — errorCode: INVALID_RESTAURANT_ID",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "404", description = "No restaurant exists with that ID — errorCode: RESTAURANT_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{restaurantId}")
  public ResponseEntity<APIResponse<Restaurant>> serveRestaurant(
      @Parameter(description = "UUID of the restaurant", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
      @PathVariable UUID restaurantId) throws APIException {
    Restaurant restaurant = restaurantService.get(restaurantId);
    return ResponseEntity.ok(APIResponse.success(restaurant));
  }

  @Operation(summary = "Get the menu for a restaurant")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Menu returned — empty list if the restaurant has no items"),
      @ApiResponse(responseCode = "400", description = "restaurantId is not a valid UUID — errorCode: INVALID_RESTAURANT_ID",
          content = @Content(schema = @Schema(implementation = ApiError.class))),
      @ApiResponse(responseCode = "404", description = "No restaurant exists with that ID — errorCode: RESTAURANT_NOT_FOUND",
          content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{restaurantId}/menu")
  public ResponseEntity<APIResponse<List<MenuItem>>> serveRestaurantMenu(
      @Parameter(description = "UUID of the restaurant", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
      @PathVariable UUID restaurantId) throws APIException {
    List<MenuItem> menu = menuService.getMenuForRestaurant(restaurantId);
    return ResponseEntity.ok(APIResponse.success(menu));
  }
}
