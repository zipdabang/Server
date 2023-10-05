package zipdabang.server.auth.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.auth.provider.TokenProvider;
import zipdabang.server.apiPayload.exception.handler.JwtAuthenticationException;
import zipdabang.server.redis.service.RedisService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest httpServletRequest = request;
        String jwt = tokenProvider.resolveToken(httpServletRequest);
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt, TokenProvider.TokenType.ACCESS)){

            // jwt는 정상적인 형태이나, 로그아웃 한 토큰인가?
            if(!redisService.validateLoginToken(jwt)) {
                logger.error("이미 로그아웃 된 토큰 발견");
                throw new JwtAuthenticationException(CommonStatus.JWT_FORBIDDEN);
            }
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }else{
            throw new JwtAuthenticationException(CommonStatus.JWT_TOKEN_NOT_FOUND);
        }
        filterChain.doFilter(httpServletRequest, response);
    }
}
