package boiz.shop._2BShop.api.response;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record PaginatedResponse<T>(
        List<T> items,
        int currentPage,
        int pageSize,
        int totalPages,
        long totalItems,
        boolean hasNext,
        boolean hasPrevious) {

    public static <S, T> PaginatedResponse<T> from(Page<S> page, Function<S, T> mapper) {
        return new PaginatedResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext(),
                page.hasPrevious());
    }
}
