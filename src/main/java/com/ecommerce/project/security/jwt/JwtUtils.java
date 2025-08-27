package com.ecommerce.project.security.jwt;


import com.ecommerce.project.security.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger= LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.ecom.app.jwtCookieName}")
    private String jwtCookie;

    //getting jwt from headers
//    public String getJwtFromHeader(HttpServletRequest request){
//        String bearerToken=request.getHeader("Authorization");
//        logger.debug("Authorization Header: {}",bearerToken);
//        if(bearerToken!=null && bearerToken.startsWith("Bearer ")){
//            return bearerToken.substring(7);
//        }
//        return null;
//    }


    //What we have done is here, instead of sending in a token like we had string JWT token over here, initially, we changed this to response cookie. JWT cookie. And instead of calling generate JWT token from username and instead of passing username over here I'm simply calling generate JWT cookie. And that method that we were calling over there okay Generate JWT token from username. We are calling it within generate JWT cookie. So what is happening is we are creating a JWT token and we are sending it as a response cookie, which we are accepting over here. And then this particular cookie, we are setting it as a header over here as you can see. Okay. And then in the body of this response we are sending in the entire response okay. One optional thing you can do is you can get rid of or if you go to user info response you can get rid
    public String getJwtFromCookies(HttpServletRequest request){
        Cookie cookie= WebUtils.getCookie(request,jwtCookie);
        if(cookie!=null){
//            System.out.println("COOKIE: "+ cookie.getValue());
            return cookie.getValue();
        }
        else return null;
    }



    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal){
        String jwt=generateTokenFromUsername(userPrincipal.getUsername());
        ResponseCookie cookie=ResponseCookie.from(jwtCookie,jwt)
                .path("/api")
                .maxAge(24*60*60)
                .httpOnly(false)
                .build();

        return cookie;
    }

    public ResponseCookie getCleanJwtCookie(){
        ResponseCookie cookie=ResponseCookie.from(jwtCookie,null)
                .path("/api")
                .build();

        return cookie;
    }
    //generating token from username
    public String generateTokenFromUsername(String username){
//        String username=userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime()+jwtExpirationMs)))
                .signWith(key())
                .compact();
    }

    //getting username from jwt token
    public String getUserNameFromJWTToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    //generate signing key
    public Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    //validate jwt token
    public boolean validateJwtToken(String authToken){
        try{

            System.out.println("Validate");
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;

        }catch (MalformedJwtException e){
            logger.error("invalid jwt token: {}",e.getMessage());
        } catch(ExpiredJwtException exception){
            logger.error("jwt token is expired: {}", exception.getMessage());
        }catch (UnsupportedJwtException e){
            logger.error("JWT token is unsupported: {}",e.getMessage());
        }catch (IllegalArgumentException e){
            logger.error("Jwt claims string is empty: {}",e.getMessage());
        }
        return false;
    }
}
