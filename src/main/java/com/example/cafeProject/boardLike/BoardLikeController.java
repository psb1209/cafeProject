package com.example.cafeProject.boardLike;

import com.example.cafeProject.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/boardLike")
public class BoardLikeController {

    private final BoardLikeService boardLikeService;
    private final MemberService memberService;

    @PostMapping("/toggle")
    public String toggle(
            @RequestParam BoardType boardType,
            @RequestParam int boardId,
            Authentication authentication
    ) {

        boardLikeService.toggle(boardType, boardId, authentication);

        //return "redirect:/noticeBoard/view/" + noticeBoardId;
        // redirect는 각 게시판 규칙에 맞게 처리
        return "redirect:/" + boardType.name().toLowerCase() + "/view/" + boardId;
    }
}

