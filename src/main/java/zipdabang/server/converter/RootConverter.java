package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.inform.HomeBanner;
import zipdabang.server.domain.inform.Notification;
import zipdabang.server.utils.converter.TimeConverter;
import zipdabang.server.web.dto.responseDto.RecipeResponseDto;
import zipdabang.server.web.dto.responseDto.RootResponseDto;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RootConverter {

    private final TimeConverter timeConverter;

    private static TimeConverter staticTimeConverter;


    @PostConstruct
    public void init() {
        this.staticTimeConverter = this.timeConverter;
    }

    public static RootResponseDto.BeverageCategoryDto toBeverageCategoryDto (Category category){
        return RootResponseDto.BeverageCategoryDto.builder()
                .categoryName(category.getName())
                .imageUrl(category.getImageUrl())
                .id(category.getId())
                .build();
    }

    public static RootResponseDto.BeverageCategoryListDto toBeverageCategoryListDto (List<Category> categoryList){

        List<RootResponseDto.BeverageCategoryDto> beverageCategoryDtoList = categoryList.stream()
                .map(category -> toBeverageCategoryDto(category)).collect(Collectors.toList());

        return RootResponseDto.BeverageCategoryListDto.builder()
                .beverageCategoryList(beverageCategoryDtoList)
                .size(beverageCategoryDtoList.size())
                .build();
    }

    public static RootResponseDto.NoticeSummaryDto toNoticeSummaryDto(Notification notification){
        return RootResponseDto.NoticeSummaryDto.builder()
                .noticeId(notification.getId())
                .title(notification.getName())
                .createdAt(staticTimeConverter.ConvertTime(notification.getCreatedAt()))
                .build();
    }

    public static RootResponseDto.NoticeListDto toNoticeListDto(List<Notification> notificationList){
        List<RootResponseDto.NoticeSummaryDto> noticeSummaryDtoList = notificationList.stream()
                .map(notification -> toNoticeSummaryDto(notification)).collect(Collectors.toList());

        return RootResponseDto.NoticeListDto.builder()
                .noticeList(noticeSummaryDtoList)
                .size(noticeSummaryDtoList.size())
                .build();
    }

    public static RootResponseDto.NoticeSpecDto toNoticeSpecDto(Notification notification){
        return RootResponseDto.NoticeSpecDto.builder()
                .description(notification.getDescription())
                .title(notification.getName())
                .createdAt(staticTimeConverter.ConvertTime(notification.getCreatedAt()))
                .build();
    }

    public static RootResponseDto.ReportListDto toReportListDto(List<Report> allReports) {
        return RootResponseDto.ReportListDto.builder()
                .reportList(allReports.stream()
                        .map(report -> toReportDto(report))
                        .collect(Collectors.toList()))
                .size(allReports.size())
                .build();
    }

    private static RootResponseDto.ReportDto toReportDto(Report report) {
        return RootResponseDto.ReportDto.builder()
                .id(report.getId())
                .reportName(report.getName())
                .build();
    }

    public static RootResponseDto.ReadPushAlarm toReadPushAlarm(){
        return RootResponseDto.ReadPushAlarm.builder()
                .deletedAt(LocalDateTime.now())
                .build();
    }

    public static RootResponseDto.BannerImageDto toRecipeBannerImageDto(List<HomeBanner> bannerList) {
        return RootResponseDto.BannerImageDto.builder()
                .bannerList(toHomeBannerDto(bannerList))
                .size(bannerList.size())
                .build();
    }

    private static List<RootResponseDto.BannerDto> toHomeBannerDto(List<HomeBanner> bannerList) {
        return bannerList.stream()
                .map(banner -> RootResponseDto.BannerDto.builder()
                        .order(banner.getInOrder())
                        .imageUrl(banner.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

}
