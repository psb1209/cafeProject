package com.example.cafeProject.communityBoardComment;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/communityBoardComment")
@RequiredArgsConstructor
@Controller
public class CommunityBoardCommentController {
    private final CommunityBoardCommentService communityBoardCommentService;

    @PostMapping("/createProc")
    public String CreateProc(
            CommunityBoardCommentDTO communityBoardCommentDTO,
            Model model,
            Authentication authentication
    ){
            try{
                UserDetails userDetails=(UserDetails)authentication.getPrincipal();
                communityBoardCommentService.setInsert(communityBoardCommentDTO,userDetails);
                return "redirect:/communityBoard/view/"+communityBoardCommentDTO.getCommunityBoardId();

            } catch (Exception e) {
                model.addAttribute("errorCode", "에러0005");
                model.addAttribute("errorMsg", e.getMessage());
                return "error/error";
            }
    }

    @GetMapping("/deleteProc/{commentId}")
    public String deleteProc(
            @PathVariable("commentId") int commentId,
            Model model
    ){
        CommunityBoardComment communityBoardComment=communityBoardCommentService.getSelectOneById(commentId);
        if(communityBoardComment==null){
            model.addAttribute("errorCode", "에러0005");
            model.addAttribute("errorMsg", "해당 댓글이 존재하지 않습니다.");
            return "error/error";
        }

        int communityBoardId=communityBoardComment.getCommunityBoard().getId();

        communityBoardCommentService.setDelete(commentId);
        return "redirect:/communityBoard/view/"+communityBoardId;
    }

    @GetMapping("/commentUpdate/{commentId}")
    public String commentUpdate(
            @PathVariable("commentId") int commentId,
            Model model
    ){
        CommunityBoardComment communityBoardComment=communityBoardCommentService.getSelectOneById(commentId);
        if(communityBoardComment==null){
            model.addAttribute("errorCode", "에러0005");
            model.addAttribute("errorMsg", "해당 댓글이 존재하지 않습니다.");
            return "error/error";
        }
        model.addAttribute("communityBoardComment",communityBoardComment);
        return "communityBoard/commentUpdate";
    }


    @PostMapping("/updateProc")
    public String updateProc(
            CommunityBoardCommentDTO communityBoardCommentDTO,
            Model model
    ){
        CommunityBoardComment communityBoardComment=communityBoardCommentService.getSelectOneById(communityBoardCommentDTO.getId());
        if(communityBoardComment==null){
            model.addAttribute("errorCode", "에러0005");
            model.addAttribute("errorMsg", "해당 댓글이 존재하지 않습니다.");
            return "error/error";
        }



        int communityBoardId=communityBoardComment.getCommunityBoard().getId();

        communityBoardCommentService.setUpdate(communityBoardCommentDTO);
        return "redirect:/communityBoard/view/"+communityBoardId;
    }
    
    
    //대댓글 추가
    @GetMapping("/replyComment/{communityBoardCommentId}")
    public String replyComment(
            @PathVariable("communityBoardCommentId") int communityBoardCommentId,
            Model model,
            Authentication authentication

    ) {
        CommunityBoardComment CommunityBoardComment = communityBoardCommentService.getSelectOneById
                (communityBoardCommentId);

        CommunityBoardComment communityBoardComment=communityBoardCommentService.getSelectOneById(communityBoardCommentId);
        model.addAttribute("communityBoardCommentId",communityBoardCommentId);
        model.addAttribute("communityBoardId", CommunityBoardComment.getCommunityBoard().getId());
        return "communityBoard/replyComment";
    }

    
    //대댓글 추가처리
    @PostMapping("/replyCreateProc")
    public String replyCreateProc(
            CommunityBoardCommentDTO communityBoardCommentDTO,
            Model model,
            Authentication authentication

    ){
        try {
           UserDetails userDetails=(UserDetails)authentication.getPrincipal();

            communityBoardCommentService.replysetInsert(communityBoardCommentDTO,userDetails);
            return "redirect:/communityBoard/view/"+communityBoardCommentDTO.getCommunityBoardId();
        } catch (Exception e) {
            model.addAttribute("errorCode", "에러0005");
            model.addAttribute("errorMsg", "해당 댓글이 해당하지 않습니다.");
            return "error/error";
        }
    }

}
