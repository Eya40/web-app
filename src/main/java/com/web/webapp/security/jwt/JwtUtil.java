package com.web.webapp.security.jwt;


import com.web.webapp.entity.User;
import com.web.webapp.exception.JwtTokenMalformedException;
import com.web.webapp.exception.JwtTokenMissingException;
import io.jsonwebtoken.*;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.token.validity}")
    private long tokenValidity;

    public String getUserName(final String token) {
        try {
            Claims body = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();

            return body.getSubject();
        } catch (Exception e) {
            System.out.println(e.getMessage() + " => " + e);
        }

        return null;
    }

    public String generateToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Claims claims = Jwts.claims().setSubject(user.getUserName());

        final long nowMillis = System.currentTimeMillis();
        final long expMillis = nowMillis + tokenValidity;

        Date exp = new Date(expMillis);

        return Jwts.builder().setClaims(claims).setIssuedAt(new Date(nowMillis)).setExpiration(exp)
                .signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
    }

    public void validateToken(final String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
        } catch (SignatureException ex) {
            throw new JwtTokenMalformedException("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            throw new JwtTokenMalformedException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            throw new JwtTokenMalformedException("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            throw new JwtTokenMalformedException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            throw new JwtTokenMissingException("JWT claims string is empty.");
        }
    }

}
