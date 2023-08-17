package zipdabang.server.converter.market;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.domain.market.MarketCategory;
import zipdabang.server.domain.market.Product;
import zipdabang.server.web.dto.responseDto.MarketResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductConverter {

    private static String makePriceData(Integer price){
        return "temp";
    }

    public static MarketResponseDto.ProductCategoryDto toProductCategoryDto(MarketCategory marketCategory){
        return MarketResponseDto.ProductCategoryDto.builder()
                .categoryId(marketCategory.getId())
                .name(marketCategory.getName())
                .categoryImageUrl(marketCategory.getImageUrl())
                .build();
    }

    public static MarketResponseDto.ProductDto toProductDto(Product product){
        return MarketResponseDto.ProductDto.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productCategoryId(product.getCategory().getId())
                .productScore(product.getStarScore())
                .productImageUrl(product.getThumbnailUrl())
                .price(makePriceData(product.getPrice()))
                .isInBasket(null)
                .isLiked(null)
                .build();
    }

    public static MarketResponseDto.WatchedProductPreviewDto toWatchedProductPreviewDto(List<MarketCategory> marketCategoryList, List<Product> productList){
        List<MarketResponseDto.ProductCategoryDto> categoryDtoList = marketCategoryList.stream()
                .map(marketCategory -> toProductCategoryDto(marketCategory))
                .collect(Collectors.toList());

        List<MarketResponseDto.ProductDto> productDtoList = productList.stream()
                .map(product -> toProductDto(product)).collect(Collectors.toList());

        return MarketResponseDto.WatchedProductPreviewDto.builder()
                .productCategoryList(categoryDtoList)
                .productList(productDtoList)
                .productListSize(productDtoList.size())
                .categoryListSize(categoryDtoList.size())
                .build();
    }

    public static MarketResponseDto.WatchedProductDto toWatchedProductDto(List<Product> productList){
        List<MarketResponseDto.ProductDto> productDtoList = productList.stream().map(
                product -> toProductDto(product)
        ).collect(Collectors.toList());

        return MarketResponseDto.WatchedProductDto.builder()
                .productList(productDtoList)
                .productListSize(productDtoList.size())
                .build();
    }
}
