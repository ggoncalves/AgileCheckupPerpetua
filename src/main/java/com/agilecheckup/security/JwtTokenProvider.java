package com.agilecheckup.security;

import static com.agilecheckup.security.TokenField.ASSESSMENT_MATRIX_ID;
import static com.agilecheckup.security.TokenField.TENANT_ID;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtTokenProvider {

  private final SecretKey key;

  public JwtTokenProvider() {
    String secret = System.getenv("JWT_SECRET_KEY");
    if (secret == null || secret.trim().isEmpty()) {
      // Use a default secret for testing/development
      secret = "ThisIsATemporarySecretKeyForDevelopmentPurposesOnly123456789012";
      System.out.println("WARNING: Using default JWT secret. Set JWT_SECRET_KEY environment variable for production.");
    }
    else {
      System.out.println("JWT_SECRET_KEY environment variable loaded successfully.");
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateInvitationToken(String tenantId, String assessmentMatrixId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(TENANT_ID.getFieldName(), tenantId);
    claims.put(ASSESSMENT_MATRIX_ID.getFieldName(), assessmentMatrixId);

    return Jwts.builder().setClaims(claims).setIssuedAt(new Date(System.currentTimeMillis())).signWith(key, SignatureAlgorithm.HS256).compact();
  }

  public Claims validateAndParseToken(String token) throws Exception {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
    catch (Exception e) {
      log.error("Failed to validate JWT token: {}", e.getMessage());
      throw new Exception("Invalid or expired invitation link");
    }
  }

  public String getTenantIdFromToken(String token) throws Exception {
    Claims claims = validateAndParseToken(token);
    return claims.get(TENANT_ID.getFieldName(), String.class);
  }

  public String getAssessmentMatrixIdFromToken(String token) throws Exception {
    Claims claims = validateAndParseToken(token);
    return claims.get(ASSESSMENT_MATRIX_ID.getFieldName(), String.class);
  }
}