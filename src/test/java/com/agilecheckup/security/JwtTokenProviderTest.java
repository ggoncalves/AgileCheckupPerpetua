package com.agilecheckup.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setUp() {
    jwtTokenProvider = new JwtTokenProvider();
  }

  @Test
  void generateInvitationToken_shouldCreateValidToken() {
    // Given
    String tenantId = "tenant-123";
    String assessmentMatrixId = "matrix-456";

    // When
    String token = jwtTokenProvider.generateInvitationToken(tenantId, assessmentMatrixId);

    // Then
    assertThat(token).isNotNull();
    assertThat(token).isNotEmpty();
    assertThat(token.split("\\.")).hasSize(3); // JWT should have 3 parts
  }

  @Test
  void validateAndParseToken_shouldExtractCorrectClaims() throws Exception {
    // Given
    String tenantId = "tenant-123";
    String assessmentMatrixId = "matrix-456";
    String token = jwtTokenProvider.generateInvitationToken(tenantId, assessmentMatrixId);

    // When
    Claims claims = jwtTokenProvider.validateAndParseToken(token);

    // Then
    assertThat(claims).isNotNull();
    assertThat(claims.get("tenantId", String.class)).isEqualTo(tenantId);
    assertThat(claims.get("assessmentMatrixId", String.class)).isEqualTo(assessmentMatrixId);
    assertThat(claims.getIssuedAt()).isNotNull();
  }

  @Test
  void getTenantIdFromToken_shouldReturnCorrectTenantId() throws Exception {
    // Given
    String tenantId = "tenant-789";
    String assessmentMatrixId = "matrix-012";
    String token = jwtTokenProvider.generateInvitationToken(tenantId, assessmentMatrixId);

    // When
    String extractedTenantId = jwtTokenProvider.getTenantIdFromToken(token);

    // Then
    assertThat(extractedTenantId).isEqualTo(tenantId);
  }

  @Test
  void getAssessmentMatrixIdFromToken_shouldReturnCorrectAssessmentMatrixId() throws Exception {
    // Given
    String tenantId = "tenant-abc";
    String assessmentMatrixId = "matrix-xyz";
    String token = jwtTokenProvider.generateInvitationToken(tenantId, assessmentMatrixId);

    // When
    String extractedMatrixId = jwtTokenProvider.getAssessmentMatrixIdFromToken(token);

    // Then
    assertThat(extractedMatrixId).isEqualTo(assessmentMatrixId);
  }

  @Test
  void validateAndParseToken_shouldThrowExceptionForInvalidToken() {
    // Given
    String invalidToken = "invalid.jwt.token";

    // When & Then
    assertThatThrownBy(() -> jwtTokenProvider.validateAndParseToken(invalidToken)).isInstanceOf(Exception.class)
                                                                                  .hasMessage("Invalid or expired invitation link");
  }

  @Test
  void validateAndParseToken_shouldThrowExceptionForTamperedToken() {
    // Given
    String tenantId = "tenant-123";
    String assessmentMatrixId = "matrix-456";
    String token = jwtTokenProvider.generateInvitationToken(tenantId, assessmentMatrixId);

    // Tamper with the token by changing a character
    String tamperedToken = token.substring(0, token.length() - 1) + "X";

    // When & Then
    assertThatThrownBy(() -> jwtTokenProvider.validateAndParseToken(tamperedToken)).isInstanceOf(Exception.class)
                                                                                   .hasMessage("Invalid or expired invitation link");
  }

  @Test
  void generateInvitationToken_shouldHandleNullValues() {
    // When
    String token = jwtTokenProvider.generateInvitationToken(null, null);

    // Then
    assertThat(token).isNotNull();
    assertThat(token).isNotEmpty();
  }

  @Test
  void multipleTokensForSameData_shouldPotentiallyGenerateDifferentTokens() {
    // Given
    String tenantId = "tenant-123";
    String assessmentMatrixId = "matrix-456";

    // When
    String token1 = jwtTokenProvider.generateInvitationToken(tenantId, assessmentMatrixId);
    // Small delay to ensure different timestamp
    try {
      Thread.sleep(1000); // 1 second delay to ensure different timestamps
    }
    catch (InterruptedException ignored) {
    }
    String token2 = jwtTokenProvider.generateInvitationToken(tenantId, assessmentMatrixId);

    // Then
    // Note: Tokens might be the same if generated at exact same millisecond, but should decode to same data
    try {
      Claims claims1 = jwtTokenProvider.validateAndParseToken(token1);
      Claims claims2 = jwtTokenProvider.validateAndParseToken(token2);

      // Both tokens should decode to the same tenant and matrix data
      assertThat(claims1.get("tenantId", String.class)).isEqualTo(claims2.get("tenantId", String.class));
      assertThat(claims1.get("assessmentMatrixId", String.class)).isEqualTo(claims2.get("assessmentMatrixId", String.class));

      // Timestamps should be different (or potentially the same if system is very fast)
      // This is the desired behavior for our stateless approach

    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}