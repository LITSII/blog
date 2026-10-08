package com.litsii.blog.post;

import com.litsii.blog.post.PostDtos.*;
import org.springframework.web.bind.annotation.*;

/** 공개 API: 발행된 글만 조회 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<PostSummary> list(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return service.listPublished(page, size);
    }

    @GetMapping("/{id}")
    public PostDetail get(@PathVariable Long id) {
        return service.getPublished(id);
    }
}
