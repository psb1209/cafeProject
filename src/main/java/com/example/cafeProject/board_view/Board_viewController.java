/*
package com.example.cafeProject.board_view;

import com.example.cafeProject.like.LikeDTO;
import com.example.cafeProject.like.LikeService;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/like")
@RequiredArgsConstructor
@Controller
public class Board_viewController {

    private final LikeService likeService;
    private final MemberService memberService;

    @PostMapping("/createProc")
    public String createProc(
            LikeDTO likeDTO,
            Model model,
            Authentication authentication
    ) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Member member = likeService.selectByUsername(userDetails.getUsername());
            likeDTO.setUserId(member.getId());
            likeService.createProc(likeDTO);

            if (likeDTO.getCommunityBoardNumber() != null)
                return "redirect:/communityBoard/view/" + likeDTO.getCommunityBoardNumber();
            if (likeDTO.getNoticeBoardNumber() != null)
                return "redirect:/noticeBoard/view/" + likeDTO.getNoticeBoardNumber();
            if (likeDTO.getInformationBoardNumber() != null)
                return "redirect:/informationBoard/view/" + likeDTO.getInformationBoardNumber();
            if (likeDTO.getOperationBoardNumber() != null)
                return "redirect:/operationBoard/view/" + likeDTO.getOperationBoardNumber();

            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("errCode", "err0001");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }
}
*/
