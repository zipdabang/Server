package zipdabang.server.auth.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.JwtAuthenticationException;
import zipdabang.server.domain.Member;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class TokenProvider implements InitializingBean {

    private final Logger LOGGER = LoggerFactory.getLogger(TokenProvider.class);

    private final String AUTHORITIES_KEY;

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final String secret;

    private final long accessTokenValidityInMilliseconds;

    private Key key;

    public enum TokenType{
        ACCESS, REFRESH;
    }

    public TokenProvider(@Value("${jwt.secret}") String secret,
                         @Value("${jwt.authorities-key}") String authoritiesKey,
                         @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInMilliseconds){
        this.secret = secret;
        this.AUTHORITIES_KEY = authoritiesKey;
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long memberId, String socialType, String socialId, Collection<? extends GrantedAuthority> authorities){
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .claim(AUTHORITIES_KEY, authorities)
                .claim("socialType", socialType)
                .claim("socialID", socialId)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public String createAccessToken(Long memberId,String phoneNum, Collection<? extends GrantedAuthority> authorities){
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .claim(AUTHORITIES_KEY, authorities)
                .claim("phoneNum", phoneNum)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public Authentication getAuthentication(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token, TokenType type) throws JwtAuthenticationException {
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e){
            throw new JwtAuthenticationException(Code.JWT_BAD_REQUEST);
        }catch (ExpiredJwtException e){
            if (type == TokenType.ACCESS) throw new JwtAuthenticationException(Code.JWT_ACCESS_TOKEN_EXPIRED);
            else throw new JwtAuthenticationException(Code.JWT_REFRESH_TOKEN_EXPIRED);
        }catch (UnsupportedJwtException e){
            throw new JwtAuthenticationException(Code.JWT_UNSUPPORTED_TOKEN);
        }catch (IllegalArgumentException e){
            throw new JwtAuthenticationException(Code.JWT_BAD_REQUEST);
        }
    }

    public Long validateAndReturnId(String token) throws JwtAuthenticationException{
        try{
            Claims body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return Long.valueOf(body.getSubject());
        }catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e){
            throw new JwtAuthenticationException(Code.JWT_BAD_REQUEST);
        }catch (UnsupportedJwtException e){
            throw new JwtAuthenticationException(Code.JWT_UNSUPPORTED_TOKEN);
        }catch (IllegalArgumentException e){
            throw new JwtAuthenticationException(Code.JWT_BAD_REQUEST);
        }
    }

    public String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
