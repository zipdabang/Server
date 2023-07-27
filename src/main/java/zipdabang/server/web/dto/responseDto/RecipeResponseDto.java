package zipdabang.server.web.dto.responseDto;


import lombok.*;

import java.time.LocalDateTime;

public class RecipeResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipeStatusDto{
        private Long recipeId;
        private LocalDateTime calledAt;
    }
}
