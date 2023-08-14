package zipdabang.server.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.JwtAuthenticationException;
import zipdabang.server.base.exception.handler.MemberException;
import zipdabang.server.domain.member.Member;
import zipdabang.server.redis.domain.LoginStatus;
import zipdabang.server.redis.domain.RefreshToken;
import zipdabang.server.redis.repository.LoginStatusRepository;
import zipdabang.server.redis.repository.RefreshTokenRepository;
import zipdabang.server.repository.memberRepositories.MemberRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RedisServiceImpl implements RedisService {

    private final MemberRepository memberRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final LoginStatusRepository loginStatusRepository;

    @Override
    @Transactional
    public String generateRefreshToken(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new MemberException(Code.MEMBER_NOT_FOUND));

        String token = UUID.randomUUID().toString();
        Long memberId = member.getMemberId();

        LocalDateTime currentTime = LocalDateTime.now();

        // test를 할 때는 plus 인자를 짧게
        LocalDateTime expireTime = currentTime.plus(2, ChronoUnit.WEEKS);

        return refreshTokenRepository.save(
                RefreshToken.builder()
                        .memberId(memberId)
                        .token(token)
                        .expireTime(expireTime).build()
        ).getToken();
    }

    @Override
    @Transactional
    public String reGenerateRefreshToken(String refreshToken) {
        RefreshToken findRefreshToken = refreshTokenRepository.findById(refreshToken).orElseThrow(() -> new JwtAuthenticationException(Code.JWT_REFRESH_TOKEN_EXPIRED));
        LocalDateTime expireTime = findRefreshToken.getExpireTime();
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime expireDeadLine = current.plusMinutes(30);

        Member member = memberRepository.findById(findRefreshToken.getMemberId()).orElseThrow(() -> new MemberException(Code.MEMBER_NOT_FOUND));

        if(current.isAfter(expireTime))
            throw new JwtAuthenticationException(Code.JWT_REFRESH_TOKEN_EXPIRED);

        // 새로 발급할 accessToken보다 refreshToken이 먼저 만료 될 경우인가?
        if(expireTime.isAfter(expireDeadLine))
            return findRefreshToken.getToken();
        else
            return generateRefreshToken(member.getEmail());
    }

    @Override
    @Transactional
    public void saveLoginStatus(Long memberId, String accessToken) {
        loginStatusRepository.save(
                LoginStatus.builder()
                        .accessToken(accessToken)
                        .memberId(memberId)
                        .build()
        );
    }

    @Override
    @Transactional
    public void resolveLogout(String accessToken) {
        LoginStatus loginStatus = loginStatusRepository.findById(accessToken).get();
        loginStatusRepository.delete(loginStatus);
    }

    @Override
    public Boolean validateLoginToken(String accessToken) {
        return loginStatusRepository.findById(accessToken).isPresent();
    }
}
