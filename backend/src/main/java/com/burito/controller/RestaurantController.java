package com.burito.controller;

import com.burito.controller.views.APIResponse;
import com.burito.controller.views.ApiError;
import com.burito.domain.MenuItem;
import com.burito.domain.Restaurant;
import com.burito.exceptions.APIException;
import com.burito.service.MenuService;
import com.burito.service.RestaurantService;
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

  @Operation(summary = "List all restaurants")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Returns the full restaurant list")
  })
  @GetMapping("/")
  public ResponseEntity<APIResponse<List<Restaurant>>> serveAllRestaurant() {
    List<Restaurant> restaurantList = restaurantService.list();
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
