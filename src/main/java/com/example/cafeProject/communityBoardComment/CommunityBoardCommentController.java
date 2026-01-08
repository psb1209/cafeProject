package com.example.cafeProject.communityBoardComment;

import com.example.cafeProject.communityBoard.CommunityBoardDTO;
import com.example.cafeProject.communityBoard.CommunityBoardService;
import com.example.cafeProject.member.Member;

import com.example.cafeProject.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequiredArgsConstructor
@RequestMapping("/communityBoardComment")
@Controller
public class CommunityBoardCommentController {

    private final CommunityBoardService communityBoardService;
    private final CommunityBoardCommentService communityBoardCommentService;
    private final MemberService memberService;


    String dirName = "communityBoard";

    @PostMapping("/createProc")
    public String createProc(
            Model model,
            CommunityBoardCommentDTO communityBoardCommentDTO,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal User user
    ) {
        try {
            Member member = memberService.viewCurrentMember();
            communityBoardCommentDTO.setMemberId(member.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1110");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            CommunityBoardDTO communityBoardDTO = new CommunityBoardDTO();
            communityBoardDTO.setId(communityBoardCommentDTO.getCommunityBoardId());
            communityBoardService.getSelectOneById(communityBoardDTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            boolean isUpgraded = communityBoardCommentService.setInsert(communityBoardCommentDTO, user);
            if(isUpgraded) { //등급 상승시 "msg" 데이터를 같이 리다이렉트 시킴
                redirectAttributes.addFlashAttribute("msg","축하합니다! 등급이 올랐습니다!🎉"); //윈도우 로고 키(⊞) + 마침표(.) --> 임티창
                return "redirect:/communityBoard/view/" + communityBoardCommentDTO.getCommunityBoardId();
            }
            return "redirect:/communityBoard/view/" + communityBoardCommentDTO.getCommunityBoardId(); //등급 안올랐으면 걍 조용히 이동

        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 댓글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    @PostMapping("/deleteProc")
    public String deleteProc(
            Model model,
            CommunityBoardCommentDTO communityBoardCommentDTO
    ) {

        try {
            CommunityBoardDTO communityBoardDTO = new CommunityBoardDTO();
            communityBoardDTO.setId(communityBoardCommentDTO.getCommunityBoardId());
            communityBoardService.getSelectOneById(communityBoardDTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            communityBoardCommentService.setDelete(communityBoardCommentDTO);
            return "redirect:/" + dirName + "/view/" + communityBoardCommentDTO.getCommunityBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage()); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        } catch (Exception e) { // 모든 예외는 Exception 상속 받기에 Exception이 나중에 처리되어야 함
            model.addAttribute("errCode", "err2242");
            model.addAttribute("errMsg", "삭제 중 문제가 발생했습니다."); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        }
    }

    @PostMapping("/update")
    public String update(
            CommunityBoardCommentDTO communityBoardCommentDTO,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            CommunityBoardComment communityBoardCommentUpdate = communityBoardCommentService.getCommunityBoardCommentId(communityBoardCommentDTO);
            redirectAttributes.addFlashAttribute("communityBoardCommentUpdate", communityBoardCommentUpdate);
            return "redirect:/communityBoard/view/" + communityBoardCommentDTO.getCommunityBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }


    @PostMapping("/updateProc")
    public String updateProc(
            Model model,
            CommunityBoardCommentDTO communityBoardCommentDTO
    ) {
        try {
            CommunityBoardDTO communityBoardDTO = new CommunityBoardDTO();
            communityBoardDTO.setId(communityBoardCommentDTO.getCommunityBoardId());
            communityBoardService.getSelectOneById(communityBoardDTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
        
        try {
            communityBoardCommentService.setUpdate(communityBoardCommentDTO);
            return "redirect:/" + dirName + "/view/" + communityBoardCommentDTO.getCommunityBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage()); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "err2242");
            model.addAttribute("errMsg", "수정 중 문제가 발생했습니다."); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        }
    }

    //대댓글 추가
/*
    @GetMapping("/replyComment/{communityBoardCommentId}")
    public String replyComment(
            @PathVariable int communityBoardCommentId,
            CommunityBoardCommentDTO communityBoardCommentDTO,
            Model model,
            Authentication authentication
    ) {
        CommunityBoardComment communityBoardComment = communityBoardCommentService.getCommunityBoardCommentId(communityBoardCommentDTO);
        model.addAttribute("communityBoardComment",communityBoardComment);
        return "communityBoard/replyComment";
    }
*/


    //대댓글 추가처리
    @PostMapping("/replyProc")
    public String replyProc(
            CommunityBoardCommentDTO communityBoardCommentDTO,
            Model model,
            Authentication authentication
    ){
        try {
            UserDetails userDetails=(UserDetails)authentication.getPrincipal();

            communityBoardCommentService.replySetInsert(communityBoardCommentDTO, userDetails);
            return "redirect:/communityBoard/view/" + communityBoardCommentDTO.getCommunityBoardId();
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
