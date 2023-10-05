package zipdabang.server.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.auth.provider.TokenProvider;
import zipdabang.server.apiPayload.exception.handler.RootException;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.inform.Notification;
import zipdabang.server.domain.member.Member;
import zipdabang.server.firebase.fcm.service.FirebaseService;
import zipdabang.server.repository.CategoryRepository;
import zipdabang.server.repository.NotificationRepository;
import zipdabang.server.repository.ReportRepository;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.service.RootService;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RootServiceImpl implements RootService {

    private final CategoryRepository categoryRepository;

    private final MemberRepository memberRepository;

    private final NotificationRepository notificationRepository;

    private final ReportRepository reportRepository;

    private final TokenProvider tokenProvider;

    private final FirebaseService firebaseService;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Boolean autoLoginService(String authorizationHeader) {

        Boolean result = null;
        if(authorizationHeader == null)
            result = false;
        else{
            String token = authorizationHeader.substring(7);
            Long memberId = tokenProvider.validateAndReturnSubject(token);
            if (memberId.equals(0L))
                result = true;
            else if (memberId < 0L){
                if (memberId.equals(-1L))
                    throw new RootException(CommonStatus.JWT_BAD_REQUEST);
                else if (memberId.equals(-2L))
                    throw new RootException(CommonStatus.JWT_ACCESS_TOKEN_EXPIRED);
                else if (memberId.equals(-3L))
                    throw new RootException(CommonStatus.JWT_UNSUPPORTED_TOKEN);
                else if (memberId.equals(-4L))
                    throw new RootException(CommonStatus.JWT_BAD_REQUEST);
            }
            else{
                Member member = memberRepository.findById(memberId).orElseThrow(() -> new RootException(CommonStatus.MEMBER_NOT_FOUND));
                if(member.getAge() == null || member.getNickname() == null || member.getName() == null || member.getGender() == null)
                    result = false;
                else
                    result = true;
            }
        }

        return result;
    }

    @Override
    public List<Notification> notificationList() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Notification findNotification(Long notificationId) {
        return notificationRepository.findById(notificationId).get();
    }

    @Override
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @Override
    public void testFCMService(String fcmToken) throws IOException
    {
        String title = "집다방 FCM 테스트";
        String body = "되나? 되나? 되나? 되나?";
        String targetView = "레시피";
        String targetPK = "1";
        String targetNotification = "2";
        firebaseService.sendMessageTo(fcmToken,title,body,targetView,targetPK,targetNotification);
    }
}
