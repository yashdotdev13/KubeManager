package com.kubemanager.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PageResult<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final int totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;
}
