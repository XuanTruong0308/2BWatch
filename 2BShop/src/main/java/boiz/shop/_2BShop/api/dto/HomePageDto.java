package boiz.shop._2BShop.api.dto;

import java.util.List;

public record HomePageDto(
        List<ProductCardDto> bestSellers,
        List<ProductCardDto> newestProducts,
        List<ProductCardDto> biggestDiscounts,
        List<OptionDto> brands,
        List<OptionDto> categories) {
}
