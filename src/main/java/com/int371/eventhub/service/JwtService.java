package com.int371.eventhub.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.int371.eventhub.entity.MemberEvent;
import com.int371.eventhub.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String SECRET_KEY;

    @Value("${app.jwt.expiration-ms}")
    private long EXPIRATION_TIME;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        // extraClaims.put("firstName", user.getFirstName());
        extraClaims.put("accountRole", user.getRole().name());
        extraClaims.put("tokenRole", user.getRole().name()); 

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateSurveyToken(User user ,MemberEvent mu) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        // extraClaims.put("firstName", user.getFirstName());
        extraClaims.put("eventId", mu.getEvent().getId());
        extraClaims.put("roleInEvent", mu.getEventRole());
        extraClaims.put("accountRole", user.getRole());
        extraClaims.put("tokenRole", "SURVEY_GUEST"); 
        extraClaims.put("isRestricted", true);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 30 ))) // 30 minutes
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Integer extractUserId(String token) {
        final Claims claims = extractAllClaims(token);
        System.out.println("Claims: " + claims);
        return claims.get("userId", Integer.class);
    }
    // public String extractUserEmail(String token) {
    //     final Claims claims = extractAllClaims(token);
    //     return claims.getSubject();   
    // }
}