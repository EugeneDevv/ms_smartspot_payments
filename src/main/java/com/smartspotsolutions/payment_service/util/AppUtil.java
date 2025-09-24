package com.smartspotsolutions.payment_service.util;

import com.smartspotsolutions.payment_service.io.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AppUtil {
    private final JwtUtil jwtUtil;

    public Pageable makePageable(Integer page, Integer size, String sortBy, boolean ascending){
        int safePage = (page == null || page < 0) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 50 : size;
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(safePage - 1, safeSize, sort);
    }

    public PagedResponse<?> toPagedResponse(Page<?> page, List<?> content) {
        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
