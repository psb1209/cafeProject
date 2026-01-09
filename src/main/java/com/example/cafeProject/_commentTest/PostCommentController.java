package com.example.cafeProject._commentTest;

import com.example.cafeProject._postTest.PostDTO;
import com.example.cafeProject._postTest.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@Controller
@RequestMapping("/cafe/{cafeCode}/postComment")
public class PostCommentController {

    private final PostCommentService postCommentService;
    private final PostService postService;

    @PostMapping("/createProc")
    public String createProc(
            @PathVariable String cafeCode,
            @RequestParam(name = "b", required = false) String boardCode,
            Model model,
            PostCommentDTO postCommentDTO,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes
    ) {
        // 게시글 존재 확인
        try {
            PostDTO postDTO = postService.viewDetailDTO(postCommentDTO.getPostId());
            if (boardCode == null || boardCode.isBlank()) boardCode = postDTO.getBoardCode();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            boolean isUpgraded = postCommentService.setInsert(postCommentDTO, user);
            if (isUpgraded) {
                redirectAttributes.addFlashAttribute("msg", "축하합니다! 등급이 올랐습니다!🎉");
            }
            return "redirect:/cafe/" + cafeCode + "/post/view/" + postCommentDTO.getPostId() + "?b=" + boardCode;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 댓글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    @PostMapping("/deleteProc")
    public String deleteProc(
            @PathVariable String cafeCode,
            @RequestParam(name = "b", required = false) String boardCode,
            Model model,
            PostCommentDTO postCommentDTO
    ) {
        try {
            PostDTO postDTO = postService.viewDetailDTO(postCommentDTO.getPostId());
            if (boardCode == null || boardCode.isBlank()) boardCode = postDTO.getBoardCode();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            postCommentService.setDelete(postCommentDTO);
            return "redirect:/cafe/" + cafeCode + "/post/view/" + postCommentDTO.getPostId() + "?b=" + boardCode;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }

    @PostMapping("/update")
    public String update(
            @PathVariable String cafeCode,
            @RequestParam(name = "b", required = false) String boardCode,
            PostCommentDTO postCommentDTO,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            PostComment postCommentUpdate = postCommentService.getPostCommentId(postCommentDTO);
            redirectAttributes.addFlashAttribute("postCommentUpdate", postCommentUpdate);

            PostDTO postDTO = postService.viewDetailDTO(postCommentDTO.getPostId());
            if (boardCode == null || boardCode.isBlank()) boardCode = postDTO.getBoardCode();

            return "redirect:/cafe/" + cafeCode + "/post/view/" + postCommentDTO.getPostId() + "?b=" + boardCode;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }

    @PostMapping("/updateProc")
    public String updateProc(
            @PathVariable String cafeCode,
            @RequestParam(name = "b", required = false) String boardCode,
            Model model,
            PostCommentDTO postCommentDTO
    ) {
        try {
            PostDTO postDTO = postService.viewDetailDTO(postCommentDTO.getPostId());
            if (boardCode == null || boardCode.isBlank()) boardCode = postDTO.getBoardCode();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            postCommentService.setUpdate(postCommentDTO);
            return "redirect:/cafe/" + cafeCode + "/post/view/" + postCommentDTO.getPostId() + "?b=" + boardCode;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "err0005");
            model.addAttribute("errMsg", "해당 댓글이 해당하지 않습니다.");
            return "error/error";
        }
    }

    @PostMapping("/replyProc")
    public String replyProc(
            @PathVariable String cafeCode,
            @RequestParam(name = "b", required = false) String boardCode,
            PostCommentDTO postCommentDTO,
            Model model,
            Authentication authentication
    ) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            PostDTO postDTO = postService.viewDetailDTO(postCommentDTO.getPostId());
            if (boardCode == null || boardCode.isBlank()) boardCode = postDTO.getBoardCode();

            postCommentService.replySetInsert(postCommentDTO, userDetails);
            return "redirect:/cafe/" + cafeCode + "/post/view/" + postCommentDTO.getPostId() + "?b=" + boardCode;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "221213321");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "에러0005");
            model.addAttribute("errMsg", "해당 댓글이 해당하지 않습니다.");
            return "error/error";
        }
    }
}
