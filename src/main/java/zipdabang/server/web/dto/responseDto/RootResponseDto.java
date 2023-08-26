package zipdabang.server.web.dto.responseDto;

import lombok.*;

import java.util.List;

public class RootResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class BeverageCategoryDto{
        private Long id;
        private String categoryName;
        private String imageUrl;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class BeverageCategoryListDto{
        List<BeverageCategoryDto> beverageCategoryList;
        Integer size;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class BannerDto{
        private Integer order;
        private String imageUrl;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class BannerImageDto {
        List<BannerDto> bannerList;
        Integer size;
    }
}
