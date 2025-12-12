package com.example.cafeProject.communityBoard;

import com.example.cafeProject.communityBoardComment.CommunityBoardComment;
import com.example.cafeProject.communityBoardComment.CommunityBoardCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/communityBoard")
@RequiredArgsConstructor
@Controller
public class CommunityBoardController {
    private final CommunityBoardService communityBoardService;
    private final CommunityBoardCommentService communityBoardCommentService;

    @GetMapping("/list")
    public String list(
            Model model,
            @PageableDefault(size=3, sort="id", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<CommunityBoard> communityBoardPage=communityBoardService.getSelectAllPage(pageable);
        model.addAttribute("communityBoardPage",communityBoardPage);
        return "communityBoard/list";
    }

    @GetMapping("/view/{id}")
    public String view(
            @PathVariable("id") int id,
            Model model,
            @PageableDefault(size=3, sort="id", direction = Sort.Direction.DESC) Pageable pageable
    ){
        try{
            CommunityBoard communityBoard =communityBoardService.getSelectOneById(id);
            communityBoardService.cntProc(communityBoard);
            model.addAttribute("communityBoard",communityBoard);


            Page<CommunityBoardComment> communityBoardCommentPage=communityBoardCommentService.communityBoardCommentPage(id,pageable);
            model.addAttribute("communityBoardCommentPage",communityBoardCommentPage);
            return "communityBoard/view";

        } catch (Exception e) {
            model.addAttribute("errorCode", "err0001");
            model.addAttribute("errorMsg", e.getMessage());
            return "error/error";
        }

    }

    @GetMapping("/create")
    public String create(){
        return "communityBoard/create";
    }

    @GetMapping("/update/{id}")
    public String update(
            Model model,
            CommunityBoardDTO communityBoardDTO
    ){
        try{
            CommunityBoard communityBoard =communityBoardService.getSelectOneById(communityBoardDTO.getId());
            model.addAttribute("communityBoard",communityBoard);
            return "communityBoard/update";
        } catch (Exception e) {
            model.addAttribute("errorCode", "err0001");
            model.addAttribute("errorMsg", e.getMessage());
            return "error/error";
        }

    }

    @GetMapping("/delete/{id}")
    public String delete(
            Model model,
            CommunityBoardDTO communityBoardDTO
    ){
        try{
            CommunityBoard communityBoard =communityBoardService.getSelectOneById(communityBoardDTO.getId());
            model.addAttribute("communityBoard",communityBoard);
            return "communityBoard/delete";
        } catch (Exception e) {
            model.addAttribute("errorCode", "err0001");
            model.addAttribute("errorMsg", e.getMessage());
            return "error/error";
        }

    }

    @PostMapping("/createProc")
    public String createProc(
            CommunityBoardDTO communityBoardDTO,
            Model model
    ){
        try{
            communityBoardService.setInsert(communityBoardDTO);
            return "redirect:/communityBoard/list";
        } catch (Exception e) {
            model.addAttribute("errorCode", "err0001");
            model.addAttribute("errorMsg", e.getMessage());
            return "error/error";
        }

    }


    @PostMapping("/updateProc")
    public String updateProc(
            CommunityBoardDTO communityBoardDTO,
            Model model
    ){
        try{
            communityBoardService.setUpdate(communityBoardDTO);
            return "redirect:/communityBoard/view/"+communityBoardDTO.getId();
        } catch (Exception e) {
            model.addAttribute("errorCode", "err0001");
            model.addAttribute("errorMsg", e.getMessage());
            return "error/error";
        }


    }

    @PostMapping("/deleteProc")
    public String deleteProc(
            CommunityBoardDTO communityBoardDTO,
            Model model
    ){  try{
        communityBoardService.setDelete(communityBoardDTO.getId());
        return "redirect:/communityBoard/list";
    } catch (Exception e) {
        model.addAttribute("errorCode", "err0001");
        model.addAttribute("errorMsg", e.getMessage());
        return "error/error";
    }

    }

}
