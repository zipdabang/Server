package zipdabang.server.web.dto.responseDto;

import lombok.*;

import java.util.List;

public class MarketResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ProductCategoryDto{
        private Long categoryId;
        private String name;
        private String categoryImageUrl;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ProductDto{
        private Long productId;
        private Long productCategoryId;
        private String productImageUrl;
        private String productName;
        private String price;
        private Float productScore;
        private Boolean isInBasket;
        private Boolean isLiked;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class WatchedProductPreviewDto{
        private List<ProductCategoryDto> productCategoryList;
        private List<ProductDto> productList;
        private Integer categoryListSize;
        private Integer productListSize;
    }

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class WatchedProductDto{
        private List<ProductDto> productList;
        Long totalElements;
        Integer currentPageElements;
        Integer totalPage;
        Boolean isFirst;
        Boolean isLast;
    }
}
