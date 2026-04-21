package com.example.membersite.controller;

import com.example.membersite.dto.PostForm;
import com.example.membersite.entity.Post;
import com.example.membersite.interceptor.LoginCheckInterceptor;
import com.example.membersite.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/boards")  //Url Prefix : 二쇱냼 ?욎뿉 怨듯넻?쇰줈 遺숇뒗 寃쎈줈
@RequiredArgsConstructor
public class PostController {

    private static final String EDIT_DENIED_MESSAGE = "본인이 작성한 글만 수정할 수 있습니다.";
    private static final String DELETE_DENIED_MESSAGE = "본인이 작성한 글만 삭제할 수 있습니다.";

    private final PostService postService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("posts", postService.findAll());
        return "boards/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "boards/new";
    }

    @PostMapping("/new")
    public String create(HttpServletRequest request,
                         @Valid @ModelAttribute PostForm postForm,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        String loginId = getLoginId(request);

        if (bindingResult.hasErrors()) {
            return "boards/new";
        }

        try {
            Post created = postService.create(loginId, postForm);
            redirectAttributes.addFlashAttribute("message", "게시글이 등록되었습니다.");
            return "redirect:/boards/" + created.getId();
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("post.invalid", exception.getMessage());
            return "boards/new";
        }
    }

    @GetMapping("/{postId}")
    public String detail(HttpServletRequest request,
                         @PathVariable Long postId,
                         Model model) {
        String loginId = getLoginId(request);
        Post post = postService.findById(postId);

        model.addAttribute("post", post);
        model.addAttribute("isOwner", postService.isOwner(loginId, postId));
        return "boards/detail";
    }

    @GetMapping("/{postId}/edit")
    public String editForm(HttpServletRequest request,
                           @PathVariable Long postId,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        String loginId = getLoginId(request);
        Post post = postService.findById(postId);

        if (isNotOwner(loginId, postId)) {
            return redirectToDetailWithError(redirectAttributes, postId, EDIT_DENIED_MESSAGE);
        }

        PostForm form = new PostForm();
        form.setTitle(post.getTitle());
        form.setContent(post.getContent());

        model.addAttribute("post", post);
        model.addAttribute("postForm", form);
        return "boards/edit";
    }

    @PostMapping("/{postId}/edit")
    public String edit(HttpServletRequest request,
                       @PathVariable Long postId,
                       @Valid @ModelAttribute PostForm postForm,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        String loginId = getLoginId(request);
        Post post = postService.findById(postId);

        if (isNotOwner(loginId, postId)) {
            return redirectToDetailWithError(redirectAttributes, postId, EDIT_DENIED_MESSAGE);
        }

        if (bindingResult.hasErrors()) {
            return renderEditView(model, post);
        }

        try {
            postService.update(loginId, postId, postForm);
            redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
            return "redirect:/boards/" + postId;
        } catch (IllegalArgumentException exception) {
            bindingResult.reject("post.invalid", exception.getMessage());
            return renderEditView(model, post);
        }
    }

    @PostMapping("/{postId}/delete")
    public String delete(HttpServletRequest request,
                         @PathVariable Long postId,
                         RedirectAttributes redirectAttributes) {
        String loginId = getLoginId(request);
        try {
            postService.delete(loginId, postId);
            redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
            return "redirect:/boards";
        } catch (IllegalArgumentException exception) {
            return redirectToDetailWithError(redirectAttributes, postId, DELETE_DENIED_MESSAGE);
        }
    }

    private boolean isNotOwner(String loginId, Long postId) {
        return !postService.isOwner(loginId, postId);
    }

    private String redirectToDetailWithError(RedirectAttributes redirectAttributes, Long postId, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:/boards/" + postId;
    }

    private String renderEditView(Model model, Post post) {
        model.addAttribute("post", post);
        return "boards/edit";
    }

    private String getLoginId(HttpServletRequest request) {
        return (String) request.getAttribute(LoginCheckInterceptor.LOGIN_ID_ATTRIBUTE);
    }
}
