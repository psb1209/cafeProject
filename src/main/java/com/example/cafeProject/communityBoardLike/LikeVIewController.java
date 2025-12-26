package com.example.cafeProject.communityBoardLike;

import com.example.cafeProject.communityBoard.CommunityBoardService;
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
public class LikeVIewController {
    private final LikeService likeService;
    private final CommunityBoardService communityBoardService;
    private final MemberService memberService;

    @PostMapping("/createProc")
    public String createProc(
            LikeDTO likeDTO,
            Model model,
            Authentication authentication
    ) {

        try {
            //like메소드에서 보드아이디 검색 후 likeRepository에 저장까지!

            UserDetails userDetails=(UserDetails) authentication.getPrincipal();
            String username= userDetails.getUsername();

            Member member=likeService.selectByUsername(username);

            likeDTO.setUserId(member.getId());
            likeService.createProc(likeDTO);
            return "redirect:/communityBoard/view/"+likeDTO.getCommunityBoardNumber();
        } catch (Exception e) {
            model.addAttribute("errorCode", "err0001");
            model.addAttribute("errorMsg", e.getMessage());
            return "error/error";
        }


    }
}
