package com.ecommerce.UserService.authUtilities;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "U3VwZXJTZWNyZXRLZXlTdHJvbmcxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";  // 🔐 move this to env in prod

    public String generateToken(Long userId, String role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

}
