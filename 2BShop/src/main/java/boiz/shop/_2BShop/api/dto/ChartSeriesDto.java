package boiz.shop._2BShop.api.dto;

import java.util.List;

public record ChartSeriesDto(
        List<String> labels,
        List<Number> data) {
}
