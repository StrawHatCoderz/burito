package com.burito.filter;

import com.burito.service.JWTService;
import com.burito.service.UserService;
import com.burito.utils.Parser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JWTService jwtService;
  private final UserService userService;

  public JwtAuthenticationFilter(JWTService jwtService, UserService userService) {
    this.jwtService = jwtService;
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(
          HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = Parser.parseJwtToken(authHeader);
    try {
      String username = jwtService.extractUsername(token);

      if (username != null) {
        UserDetails userDetails = userService.loadUserByUsername(username);
        if (jwtService.isValidToken(token, userDetails)) {
          UsernamePasswordAuthenticationToken authenticationToken =
                  new UsernamePasswordAuthenticationToken(userDetails, null,
                          userDetails.getAuthorities());
          authenticationToken.setDetails(new WebAuthenticationDetailsSource()
                  .buildDetails(request)
          );

          SecurityContextHolder.getContext()
                  .setAuthentication(authenticationToken);
        }
      }

      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"token_expired\"}");
    } catch (JwtException e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"invalid_token\"}");
    }
  }
}
