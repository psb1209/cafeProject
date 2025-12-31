package com.example.cafeProject.noticeBoardComment2;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.noticeBoard2.NoticeBoard2DTO;
import com.example.cafeProject.noticeBoard2.NoticeBoard2Service;
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
@RequestMapping("/noticeBoardComment2")
@Controller
public class NoticeBoardComment2Controller {

    private final NoticeBoard2Service noticeBoard2Service;
    private final NoticeBoardComment2Service noticeBoardComment2Service;


    String dirName = "noticeBoard2";

    @PostMapping("/createProc")
    public String createProc(
            Model model,
            NoticeBoardComment2DTO noticeBoardComment2DTO,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal User user
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String loginId = userDetails.getUsername(); // 로그인했을 때 아이디
        try {
            Member member = noticeBoard2Service.getSelectOneByUsername(authentication);
            noticeBoardComment2DTO.setMemberId(member.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1110");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            NoticeBoard2DTO noticeBoard2DTO = new NoticeBoard2DTO();
            noticeBoard2DTO.setId(noticeBoardComment2DTO.getNoticeBoardId());
            noticeBoard2Service.getSelectOneById(noticeBoard2DTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            boolean isUpgraded = noticeBoardComment2Service.setInsert(noticeBoardComment2DTO, user);
            if(isUpgraded) { //등급 상승시 "msg" 데이터를 같이 리다이렉트 시킴
                redirectAttributes.addFlashAttribute("msg","축하합니다! 등급이 올랐습니다!🎉"); //윈도우 로고 키(⊞) + 마침표(.) --> 임티창
                return "redirect:/noticeBoard2/view/" + noticeBoardComment2DTO.getNoticeBoardId();
            }
            return "redirect:/noticeBoard2/view/" + noticeBoardComment2DTO.getNoticeBoardId(); //등급 안올랐으면 걍 조용히 이동

        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 댓글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    @PostMapping("/deleteProc")
    public String deleteProc(
            Model model,
            NoticeBoardComment2DTO noticeBoardComment2DTO
    ) {

        try {
            NoticeBoard2DTO noticeBoard2DTO = new NoticeBoard2DTO();
            noticeBoard2DTO.setId(noticeBoardComment2DTO.getNoticeBoardId());
            noticeBoard2Service.getSelectOneById(noticeBoard2DTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            noticeBoardComment2Service.setDelete(noticeBoardComment2DTO);
            return "redirect:/" + dirName + "/view/" + noticeBoardComment2DTO.getNoticeBoardId();
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
            NoticeBoardComment2DTO noticeBoardComment2DTO,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            NoticeBoardComment2 noticeBoardComment2Update = noticeBoardComment2Service.getNoticeBoardCommentId(noticeBoardComment2DTO);
            redirectAttributes.addFlashAttribute("noticeBoardComment2Update", noticeBoardComment2Update);
            return "redirect:/noticeBoard2/view/" + noticeBoardComment2DTO.getNoticeBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }


    @PostMapping("/updateProc")
    public String updateProc(
            Model model,
            NoticeBoardComment2DTO noticeBoardComment2DTO
    ) {
        try {
            NoticeBoard2DTO noticeBoard2DTO = new NoticeBoard2DTO();
            noticeBoard2DTO.setId(noticeBoardComment2DTO.getNoticeBoardId());
            noticeBoard2Service.getSelectOneById(noticeBoard2DTO);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
        
        try {
            noticeBoardComment2Service.setUpdate(noticeBoardComment2DTO);
            return "redirect:/" + dirName + "/view/" + noticeBoardComment2DTO.getNoticeBoardId();
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
    @GetMapping("/replyComment/{noticeBoard2CommentId}")
    public String replyComment(
            @PathVariable int noticeBoard2CommentId,
            NoticeBoardComment2DTO noticeBoardComment2DTO,
            Model model,
            Authentication authentication
    ) {
        OperationBoardComment noticeBoard2Comment = noticeBoardComment2Service.getOperationBoardCommentId(noticeBoardComment2DTO);
        model.addAttribute("noticeBoard2Comment",noticeBoard2Comment);
        return "noticeBoard2/replyComment";
    }
*/


    //대댓글 추가처리
    @PostMapping("/replyProc")
    public String replyProc(
            NoticeBoardComment2DTO noticeBoardComment2DTO,
            Model model,
            Authentication authentication
    ){
        try {
            UserDetails userDetails=(UserDetails)authentication.getPrincipal();

            noticeBoardComment2Service.replysetInsert(noticeBoardComment2DTO, userDetails);
            return "redirect:/noticeBoard2/view/" + noticeBoardComment2DTO.getNoticeBoardId();
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
