package zipdabang.server.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import zipdabang.server.domain.Category;
import zipdabang.server.web.dto.responseDto.RootResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RootConverter {

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
}
