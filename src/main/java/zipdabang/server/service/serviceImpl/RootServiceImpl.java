package zipdabang.server.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.apiPayload.code.CommonStatus;
import zipdabang.server.auth.provider.TokenProvider;
import zipdabang.server.apiPayload.exception.handler.RootException;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.enums.AlarmType;
import zipdabang.server.domain.etc.BannedWord;
import zipdabang.server.domain.etc.ReservedWord;
import zipdabang.server.domain.etc.SlangWord;
import zipdabang.server.domain.inform.AlarmCategory;
import zipdabang.server.domain.inform.Notification;
import zipdabang.server.domain.inform.PushAlarm;
import zipdabang.server.domain.member.Member;
import zipdabang.server.domain.recipe.Recipe;
import zipdabang.server.firebase.fcm.service.FirebaseService;
import zipdabang.server.repository.*;
import zipdabang.server.repository.AlarmRepository.AlarmCategoryRepository;
import zipdabang.server.repository.AlarmRepository.PushAlarmRepository;
import zipdabang.server.repository.memberRepositories.MemberRepository;
import zipdabang.server.repository.recipeRepositories.RecipeRepository;
import zipdabang.server.service.RootService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

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

    private final PushAlarmRepository pushAlarmRepository;

    private final AlarmCategoryRepository alarmCategoryRepository;

    private final RecipeRepository recipeRepository;

    private final SlangWordRepository slangWordRepository;
    private final ReservedWordRepository reservedWordRepository;
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
    @Transactional
    public void testFCMService(String fcmToken) throws IOException
    {
        String title = "집다방 FCM 테스트";
        String body = "되나? 되나? 되나? 되나?";
        String targetView = AlarmType.RECIPE.toString();
        String targetPK = "120";

        Recipe recipe = recipeRepository.findById(120L).get();

        PushAlarm pushAlarm = pushAlarmRepository.save(PushAlarm.builder()
                .title(title)
                .body(body)
                .isConfirmed(false)
                .targetRecipe(recipe)
                .alarmCategory(alarmCategoryRepository.findByName(AlarmType.RECIPE).get())
                .build());


        pushAlarm.setMember(memberRepository.findById(108L).get());


        firebaseService.sendMessageTo(fcmToken,title,body,targetView,targetPK,pushAlarm.getId().toString());
    }

    @Override
    @Transactional
    public void readPushAlarm(Long PushAlarmId) {
        pushAlarmRepository.deleteById(PushAlarmId);
    }

    @Override
    public Optional<PushAlarm> findPushAlarmById(Long pushAlarmId) {
        return pushAlarmRepository.findById(pushAlarmId);
    }

    // 엑셀 파일 파싱 및 비속어/예약어 db에 저장 메소드.

//    @Override
//    @Transactional
//    public void ParseExcelFile(MultipartFile inputFile) throws IOException{
//        InputStream file = inputFile.getInputStream();
//        Workbook workbook = WorkbookFactory.create(file);
//        Sheet sheet = workbook.getSheetAt(0);
//
//        // 비속어 저장
//        for (int i=0;i<sheet.getPhysicalNumberOfRows();i++) {
//            Cell cell = sheet.getRow(i).getCell(0);
//            if (cell.getCellType().equals(CellType.BLANK)) {
//                file.close();
//                break;
//            } else if (cell.getCellType().equals(CellType.NUMERIC)) {
//                slangWordRepository.save(new SlangWord(Double.toString(cell.getNumericCellValue())));
//            } else{
//                slangWordRepository.save(new SlangWord(cell.getStringCellValue()));
//            }
//        }
//
//        // 예약어 저장
//        sheet = workbook.getSheetAt(1);
//
//        for (int i=0;i<sheet.getPhysicalNumberOfRows();i++) {
//            Cell cell = sheet.getRow(i).getCell(0);
//            if (cell.getCellType().equals(CellType.BLANK)) {
//                file.close();
//                break;
//            } else if (cell.getCellType().equals(CellType.NUMERIC)) {
//                reservedWordRepository.save(new ReservedWord(Double.toString(cell.getNumericCellValue())));
//            } else{
//                reservedWordRepository.save(new ReservedWord(cell.getStringCellValue()));
//            }
//        }
//
//        file.close();
//    }

    @Override
    public boolean isNicknameContainsSlangWord(String nickname) {
        List<SlangWord> slangWords = slangWordRepository.qExistsByWordContains(nickname);
        if (slangWords.isEmpty()) {
            return false;
        }else return true;
    }

    @Override
    public boolean isNicknameReservedWord(String nickname) {
        if (reservedWordRepository.existsByWord(nickname)) {
            return true;
        } else return false;
    }
}
