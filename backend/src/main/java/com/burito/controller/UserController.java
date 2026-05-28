package com.burito.controller;

import com.burito.controller.views.APIResponse;
import com.burito.controller.views.UserProfileView;
import com.burito.domain.User;
import com.burito.repository.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
  private final UserRepo userRepo;

  public UserController(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

  @GetMapping("/me")
  public ResponseEntity<APIResponse<UserProfileView>> getCurrentUser(
          @AuthenticationPrincipal UserDetails userDetails) {
    User user = userRepo.findUserByEmail(userDetails.getUsername());
    return ResponseEntity.ok(APIResponse.success(
            new UserProfileView(user.getUserId(), user.getEmail(), user.getFullName(), user.getCreatedAt())
    ));
  }
}
