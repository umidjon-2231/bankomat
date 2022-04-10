package com.project.bankomat.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {
    @Value("${jwt.key}")
    String salt;
    @Value("${jwt.expire}")
    Long expiredTime;

    public String generateToken(String obj){
        return generateToken(obj, expiredTime);
    }

    public String generateToken(String obj, Long expire){
        return Jwts.builder()
                .signWith(SignatureAlgorithm.HS512, salt)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+expire))
                .setSubject(obj)
                .compact();
    }
    public boolean checkToken(String token){
        try {
            Jwts.parser().setSigningKey(salt).parseClaimsJws(token);
            return true;
        }
        catch (SignatureException e){
            System.err.println("Invalid JWT signature");
        }
        catch (MalformedJwtException e){
            System.err.println("Invalid JWT token");
        }
        catch (ExpiredJwtException e){
            System.err.println("Expired JWT token");
        }
        catch (UnsupportedJwtException e){
            System.err.println("Unsupported JWT token");
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }
        return  false;
    }

    public String getSubjectFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(salt)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
