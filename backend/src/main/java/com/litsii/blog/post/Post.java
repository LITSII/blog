package com.litsii.blog.post;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String summary;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected Post() {
    }

    public Post(String title, String summary, String content, PostStatus status) {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        apply(title, summary, content, status);
    }

    public void update(String title, String summary, String content, PostStatus status) {
        this.updatedAt = Instant.now();
        apply(title, summary, content, status);
    }

    private void apply(String title, String summary, String content, PostStatus status) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        if (status == PostStatus.PUBLISHED && this.publishedAt == null) {
            this.publishedAt = Instant.now();
        }
        this.status = status;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getContent() { return content; }
    public PostStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getPublishedAt() { return publishedAt; }
}
