package com.burito.identity.filter;

import com.burito.identity.service.JWTService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JWTService jwtService;
  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    filter = new JwtAuthenticationFilter(jwtService);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldPassThroughWhenNoAuthorizationHeader() throws Exception {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldPassThroughWhenAuthHeaderDoesNotStartWithBearer() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
    var response = new MockHttpServletResponse();

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldAuthenticateAndContinueChainWithValidToken() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer valid.jwt.token");
    var response = new MockHttpServletResponse();

    when(jwtService.extractUsername("valid.jwt.token")).thenReturn("wade@test.com");
    when(jwtService.extractRole("valid.jwt.token")).thenReturn("USER");

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals("wade@test.com",
            SecurityContextHolder.getContext().getAuthentication().getName());
  }

  @Test
  void shouldContinueChainWithoutAuthWhenUsernameIsNull() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer some.token");
    var response = new MockHttpServletResponse();

    when(jwtService.extractUsername("some.token")).thenReturn(null);

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldReturn401WhenTokenIsExpired() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer expired.token");
    var response = new MockHttpServletResponse();

    when(jwtService.extractUsername("expired.token"))
            .thenThrow(new ExpiredJwtException(null, null, "token expired"));

    filter.doFilter(request, response, filterChain);

    assertEquals(401, response.getStatus());
    assertEquals("application/json", response.getContentType());
    assertEquals("{\"error\":\"token_expired\"}", response.getContentAsString());
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void shouldReturn401WhenTokenIsInvalid() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer malformed.token");
    var response = new MockHttpServletResponse();

    when(jwtService.extractUsername("malformed.token"))
            .thenThrow(new JwtException("invalid token"));

    filter.doFilter(request, response, filterChain);

    assertEquals(401, response.getStatus());
    assertEquals("application/json", response.getContentType());
    assertEquals("{\"error\":\"invalid_token\"}", response.getContentAsString());
    verify(filterChain, never()).doFilter(any(), any());
  }
}
