package com.example.membersite.entity;

import java.time.LocalDateTime;

public class Post {

    private Long id;
    private Long memberId;
    private String authorName;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Post() {
    }

    public Post(Long memberId, String title, String content) {
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Post(Long id,
                Long memberId,
                String authorName,
                String title,
                String content,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id = id;
        this.memberId = memberId;
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
