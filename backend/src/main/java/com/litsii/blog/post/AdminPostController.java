package com.litsii.blog.post;

import com.litsii.blog.post.PostDtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** 관리자 API: 로그인 필요 (+ Nginx에서 Tailscale 대역만 허용 권장) */
@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    private final PostService service;

    public AdminPostController(PostService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<PostSummary> list(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return service.listAll(page, size);
    }

    @GetMapping("/{id}")
    public PostDetail get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDetail create(@Valid @RequestBody PostRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public PostDetail update(@PathVariable Long id, @Valid @RequestBody PostRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
