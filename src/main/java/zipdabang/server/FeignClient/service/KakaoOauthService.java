package zipdabang.server.FeignClient.service;

import zipdabang.server.FeignClient.dto.OAuthInfoDto;

public interface KakaoOauthService {

    OAuthInfoDto getKakaoUserInfo(String token);
}
