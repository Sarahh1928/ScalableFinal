package com.ecommerce.UserService.authUtilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    // The secret key to validate the JWT signature - make sure to keep it secure and move it to environment variables in production
    private final String SECRET_KEY = "U3VwZXJTZWNyZXRLZXlTdHJvbmcxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    // Method to generate a JWT token
    public String generateToken(Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token expires in 1 day
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // Method to extract the userId from the JWT token
    public Long extractUserId(String token) {
        // Parse the token and extract claims
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        // Return the userId from the claims
        return claims.get("userId", Long.class);
    }

    // Method to extract the role from the JWT token
    public String extractRole(String token) {
        // Parse the token and extract claims
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        // Return the role from the claims
        return claims.get("role", String.class);
    }

    // Method to validate the token's expiration date and integrity
    public boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        return expiration.before(new Date());
    }

    // Method to validate the JWT token's signature and integrity
    public boolean validateToken(String token) {
        try {
            // This will throw an exception if the token is invalid or expired
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
