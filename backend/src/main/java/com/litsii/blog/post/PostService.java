package com.litsii.blog.post;

import com.litsii.blog.post.PostDtos.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    // ---- 공개 ----

    public PageResponse<PostSummary> listPublished(int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), clamp(size),
                Sort.by(Sort.Direction.DESC, "publishedAt"));
        return PageResponse.of(repository.findByStatus(PostStatus.PUBLISHED, pageable).map(PostSummary::from));
    }

    public PostDetail getPublished(Long id) {
        return repository.findByIdAndStatus(id, PostStatus.PUBLISHED)
                .map(PostDetail::from)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    // ---- 관리자 ----

    public PageResponse<PostSummary> listAll(int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), clamp(size),
                Sort.by(Sort.Direction.DESC, "updatedAt"));
        return PageResponse.of(repository.findAll(pageable).map(PostSummary::from));
    }

    public PostDetail get(Long id) {
        return PostDetail.from(find(id));
    }

    @Transactional
    public PostDetail create(PostRequest req) {
        Post post = new Post(req.title().strip(), blankToNull(req.summary()), req.content(), req.status());
        return PostDetail.from(repository.save(post));
    }

    @Transactional
    public PostDetail update(Long id, PostRequest req) {
        Post post = find(id);
        post.update(req.title().strip(), blankToNull(req.summary()), req.content(), req.status());
        return PostDetail.from(post);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(find(id));
    }

    private Post find(Long id) {
        return repository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
    }

    private static int clamp(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.strip();
    }
}
