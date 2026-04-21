package com.example.membersite.service;

import com.example.membersite.dto.PostForm;
import com.example.membersite.entity.Member;
import com.example.membersite.entity.Post;
import com.example.membersite.repository.PostRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final MemberService memberService;

    public PostService(PostRepository postRepository, MemberService memberService) {
        this.postRepository = postRepository;
        this.memberService = memberService;
    }

    public List<Post> findAll() {
        return postRepository.findAllOrderByIdDesc();
    }

    public Post findById(Long postId) {
        Post post = postRepository.findById(postId);
        if (post == null) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }
        return post;
    }

    public Post create(String loginId, PostForm form) {
        validate(form);
        Member member = memberService.findByLoginId(loginId);
        Post post = new Post(member.getId(), form.getTitle().trim(), form.getContent().trim());
        return postRepository.save(post);
    }

    public void update(String loginId, Long postId, PostForm form) {
        validate(form);
        Member member = memberService.findByLoginId(loginId);
        Post target = postRepository.findByIdAndMemberId(postId, member.getId());
        if (target == null) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }
        postRepository.update(postId, form.getTitle().trim(), form.getContent().trim(), LocalDateTime.now());
    }

    public void delete(String loginId, Long postId) {
        Member member = memberService.findByLoginId(loginId);
        Post target = postRepository.findByIdAndMemberId(postId, member.getId());
        if (target == null) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }
        postRepository.deleteById(postId);
    }

    public boolean isOwner(String loginId, Long postId) {
        Member member = memberService.findByLoginId(loginId);
        return postRepository.findByIdAndMemberId(postId, member.getId()) != null;
    }

    private void validate(PostForm form) {
        String title = form.getTitle() == null ? "" : form.getTitle().trim();
        String content = form.getContent() == null ? "" : form.getContent().trim();

        if (title.isEmpty()) {
            throw new IllegalArgumentException("제목을 입력하세요.");
        }
        if (content.isEmpty()) {
            throw new IllegalArgumentException("내용을 입력하세요.");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("제목은 100자 이하로 입력하세요.");
        }
        if (content.length() > 5000) {
            throw new IllegalArgumentException("내용은 5000자 이하로 입력하세요.");
        }
    }
}
