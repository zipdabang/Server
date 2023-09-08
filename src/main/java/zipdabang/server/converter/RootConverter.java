package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.domain.Category;
import zipdabang.server.domain.inform.Notification;
import zipdabang.server.utils.converter.TimeConverter;
import zipdabang.server.web.dto.responseDto.RootResponseDto;

import javax.annotation.PostConstruct;
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
}
