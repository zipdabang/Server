package zipdabang.server.FeignClient.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zipdabang.server.FeignClient.KakaoInfoFeignClient;
import zipdabang.server.FeignClient.converter.KakaoOAuthConverter;
import zipdabang.server.FeignClient.dto.KakaoSocialUserDto;
import zipdabang.server.FeignClient.dto.OAuthInfoDto;
import zipdabang.server.FeignClient.service.KakaoOauthService;


@Service
@RequiredArgsConstructor
public class KakoOauthServiceImpl implements KakaoOauthService {

    private final KakaoInfoFeignClient kakaoInfoFeignClient;

    @Override
    public OAuthInfoDto getKakaoUserInfo(String token) {
        KakaoSocialUserDto info = kakaoInfoFeignClient.getInfo(token);
        OAuthInfoDto oAuthInfo = KakaoOAuthConverter.toOAuthInfoDto(info);
        return oAuthInfo;
    }
}
