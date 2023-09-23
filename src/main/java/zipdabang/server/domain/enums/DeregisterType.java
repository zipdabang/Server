package zipdabang.server.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeregisterType {

    NOTHING_TO_BUY("사고싶은 물건이 없어요."),
    DISINTERESTED("앱을 이용하지 않아요."),
    UNCOMFORTABLE("앱 이용이 불편해요."),
    NEW_REGISTER("새 계정을 만들고 싶어요."),
    MET_RUDE_USER("비매너 유저를 만났어요."),
    OTHERS("기타"),
    INAPPROPRIATE_USER("불건전한 서비스 이용");

    private final String description;
}
