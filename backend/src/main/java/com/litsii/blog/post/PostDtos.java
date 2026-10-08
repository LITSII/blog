package com.litsii.blog.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

public final class PostDtos {

    private PostDtos() {
    }

    /** 목록용: 본문 제외 */
    public record PostSummary(Long id, String title, String summary, PostStatus status,
                              Instant publishedAt, Instant updatedAt) {
        static PostSummary from(Post p) {
            return new PostSummary(p.getId(), p.getTitle(), p.getSummary(), p.getStatus(),
                    p.getPublishedAt(), p.getUpdatedAt());
        }
    }

    public record PostDetail(Long id, String title, String summary, String content, PostStatus status,
                             Instant createdAt, Instant updatedAt, Instant publishedAt) {
        static PostDetail from(Post p) {
            return new PostDetail(p.getId(), p.getTitle(), p.getSummary(), p.getContent(), p.getStatus(),
                    p.getCreatedAt(), p.getUpdatedAt(), p.getPublishedAt());
        }
    }

    public record PostRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 500) String summary,
            @NotBlank @Size(max = 200_000) String content,
            @NotNull PostStatus status) {
    }

    /** Page 객체를 그대로 직렬화하지 않고 필요한 필드만 노출 */
    public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
        static <T> PageResponse<T> of(Page<T> page) {
            return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                    page.getTotalElements(), page.getTotalPages());
        }
    }
}
