package zipdabang.server.redis.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.auth.provider.TokenProvider;
import zipdabang.server.base.Code;
import zipdabang.server.base.exception.handler.MemberException;
import zipdabang.server.base.exception.handler.RefreshTokenExceptionHandler;
import zipdabang.server.domain.member.Member;
import zipdabang.server.redis.domain.LoginStatus;
import zipdabang.server.redis.domain.RefreshToken;
import zipdabang.server.redis.repository.LoginStatusRepository;
import zipdabang.server.redis.repository.RefreshTokenRepository;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.web.dto.requestDto.MemberRequestDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RedisServiceImpl implements RedisService {

    private final MemberRepository memberRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final LoginStatusRepository loginStatusRepository;
    
    private final TokenProvider tokenProvider;

    Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Override
    @Transactional
    public RefreshToken generateRefreshToken(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new MemberException(Code.MEMBER_NOT_FOUND));

        String token = UUID.randomUUID().toString();
        Long memberId = member.getMemberId();

        LocalDateTime currentTime = LocalDateTime.now();

        // test를 할 때는 plus 인자를 짧게
        LocalDateTime expireTime = currentTime.plus(1000, ChronoUnit.MINUTES);

        return refreshTokenRepository.save(
                RefreshToken.builder()
                        .memberId(memberId)
                        .token(token)
                        .expireTime(expireTime).build()
        );
    }

    @Override
    @Transactional
    public RefreshToken reGenerateRefreshToken(MemberRequestDto.IssueTokenDto request) {
        if(request.getRefreshToken() == null)
            throw new MemberException(Code.REFRESH_TOKEN_NOT_FOUND);
        RefreshToken findRefreshToken = refreshTokenRepository.findById(request.getRefreshToken()).orElseThrow(() -> new RefreshTokenExceptionHandler(Code.JWT_REFRESH_TOKEN_EXPIRED));
        LocalDateTime expireTime = findRefreshToken.getExpireTime();
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime expireDeadLine = current.plusSeconds(20);

        Member member = memberRepository.findById(findRefreshToken.getMemberId()).orElseThrow(() -> new MemberException(Code.MEMBER_NOT_FOUND));

        if(current.isAfter(expireTime)) {
            logger.error("이미 만료된 리프레시 토큰 발견");
            throw new RefreshTokenExceptionHandler(Code.JWT_REFRESH_TOKEN_EXPIRED);
        }

        // 새로 발급할 accessToken보다 refreshToken이 먼저 만료 될 경우인가?
        if(expireTime.isAfter(expireDeadLine)) {
            logger.info("기존 리프레시 토큰 발급");
            return findRefreshToken;
        }
        else {
            logger.info("accessToken보다 먼저 만료될 예정인 리프레시 토큰 발견");
            deleteRefreshToken(request.getRefreshToken());
            return generateRefreshToken(member.getEmail());
        }
    }

    @Override
    @Transactional
    public String saveLoginStatus(Long memberId, String accessToken) {
        loginStatusRepository.save(
                LoginStatus.builder()
                        .accessToken(accessToken)
                        .memberId(memberId)
                        .build()
        );

        return accessToken;
    }

    @Override
    @Transactional
    public void resolveLogout(String accessToken) {
        LoginStatus loginStatus = loginStatusRepository.findById(accessToken).get();
        loginStatusRepository.delete(loginStatus);
    }

    @Override
    public void deleteRefreshToken(String refreshToken) {
        Optional<RefreshToken> target = refreshTokenRepository.findById(refreshToken);
        if(target.isPresent())
            refreshTokenRepository.delete(target.get());
    }

    @Override
    public Boolean validateLoginToken(String accessToken) {
        Long aLong = tokenProvider.validateAndReturnSubject(accessToken);
        if(aLong == 0L)
            return true;
        return loginStatusRepository.findById(accessToken).isPresent();
    }
}
