package zipdabang.server.sms.dto;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneAuthDto{
    String phoneNum;
    LocalDateTime sendTime;
    Integer authNum;
}