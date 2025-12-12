package com.example.cafeProject.noticeBoardComment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/noticeBoardComment")
@Controller
public class NoticeBoardCommentController {
    private final NoticeBoardCommentService noticeBoardCommentService;


    @PostMapping("/createProc")
    public String createProc(
            Model model,
            NoticeBoardCommentDTO noticeBoardCommentDTO
    ) {
        try {
            noticeBoardCommentService.createProc(noticeBoardCommentDTO);
            return "redirect:/noticeBoard/view/" + noticeBoardCommentDTO.getNoticeBoardId();
        } catch (Exception e) {
            model.addAttribute("errorCode", "err0002");
            model.addAttribute("errorMsg", e.getMessage());
            return "error/error";
        }
    }

    //    @GetMapping("/sakjeProc/{commentId}/{guestBookId}")
    @GetMapping("/deleteProc/{noticeBoardCommentId}")
    public String deleteProc(Model model,
                            @PathVariable("noticeBoardCommentId") int noticeBoardCommentId
                            //@PathVariable("guestBookId") int guestBookId
    ) {

        NoticeBoardComment noticeBoardComment = noticeBoardCommentService.view(noticeBoardCommentId);
        if (noticeBoardComment == null) {
            model.addAttribute("errorCode", "err0002");
            model.addAttribute("errorMsg", "해당 댓글이 존재하지 않습니다.");
            return "error/error";
        }

        int noticeBoardId = noticeBoardComment.getNoticeBoard().getId();
        noticeBoardCommentService.deleteProc(noticeBoardCommentId);
        return "redirect:/noticeBoard/view/" + noticeBoardId + "#noticeBoardCommentList";
    }
}
