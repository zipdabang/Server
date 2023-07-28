package zipdabang.server.domain.enums;

import lombok.Getter;

@Getter
public enum OrderState {

    결제전,
    결제완료,
    배송준비,
    배송시작,
    도착,
    교환,
    환불
}
