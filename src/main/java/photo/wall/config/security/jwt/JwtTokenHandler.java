package photo.wall.config.security.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import photo.wall.config.security.user.AuthenticatedUser;

@Service
public class JwtTokenHandler {

    private final Logger logger = LogManager.getLogger(JwtTokenHandler.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.claim.userAuthorities}")
    private String userAuthoritiesClaimKey;

    @Value("${jwt.claim.userId}")
    private String userIdClaimKey;

    @Value("${app.name}")
    private String appName;


    public String generateToken(AuthenticatedUser user) {
        // Generate private claims
        Map<String, Object> privateClaims = new HashMap<>();
        privateClaims.put(userIdClaimKey, user.getId());
        privateClaims.put(userAuthoritiesClaimKey, user.getAuthorities());

        return Jwts.builder()
                // Add private claims (this needs to come first or it will override everything else)
                .setClaims(privateClaims)

                // Random ID generated
                .setId(UUID.randomUUID().toString())

                // App is the issuer and also intended audience. User is principal subject
                .setIssuer(appName)
                .setAudience(appName)
                .setSubject(user.getUsername())

                // Setup timestamp and validity
                .setIssuedAt(new Date())
                .setNotBefore(new Date())
                .setExpiration(generateExpirationDate())

                // Sign and generate token
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }


    public Boolean isTokenValid(String token) {
        try {
            // Check JWT signature and audience
            Jws<Claims> jwt =  Jwts.parser()
                    .setSigningKey(secret)
                    .requireAudience(appName)
                    .parseClaimsJws(token);

            // Make sure audience file is

            // Make sure JWT is still valid  (notBefore < now < expiration)
            return jwt.getBody().getNotBefore().before(new Date()) && jwt.getBody().getExpiration().after(new Date());
        } catch (Exception ex) {
            logger.error("JWT not properly signed or with wrong audience : " + token);
            return false;
        }
    }

    public AuthenticatedUser parseToken(String token) {
        // First, validate token
        if (!isTokenValid(token)) {
            throw new IllegalArgumentException("JWT not valid : " + token);
        }

        // Extract claims
        Jws<Claims> jwt =  Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
        Claims claims = jwt.getBody();

        // Build User from JWT
        Set<GrantedAuthority> grantedAuthorities = (Set<GrantedAuthority>)((List)claims.get(userAuthoritiesClaimKey)).stream()
                .map(elem -> ((Map)elem).get("authority"))
                .map(authority -> new SimpleGrantedAuthority((String)authority))
                .collect(Collectors.toSet());

        AuthenticatedUser user = new AuthenticatedUser(Long.valueOf((Integer)claims.get(userIdClaimKey)), claims.getSubject(),
                null, null, grantedAuthorities, true);

        return user;
    }

    /*public String getUsernameFromToken(String token) {
        String username;
        try {
            final Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    public Date getCreatedDateFromToken(String token) {
        Date created;
        try {
            final Claims claims = getClaimsFromToken(token);
            created = new Date((Long) claims.get(CLAIM_KEY_CREATED));
        } catch (Exception e) {
            created = null;
        }
        return created;
    }

    public Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            final Claims claims = getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            expiration = null;
        }
        return expiration;
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    */

   /*

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

   public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
        final Date created = getCreatedDateFromToken(token);
        return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset) && !isTokenExpired(token);
    }

    public String refreshToken(String token) {
        String refreshedToken;
        try {
            final Claims claims = getClaimsFromToken(token);
            claims.put(CLAIM_KEY_CREATED, new Date());
            refreshedToken = generateToken(claims);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }*/
}
