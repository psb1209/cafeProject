package com.example.cafeProject.informationBoardComment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/informationBoardComment")
@RequiredArgsConstructor
@Controller
public class InformationBoardCommentController {

    private final InformationBoardCommentService informationBoardCommentService;


    @PostMapping("/createProc")
    public String createProc(InformationBoardCommentDTO informationBoardCommentDTO) {
        return "redirect:/informationBoard/view/" + informationBoardCommentDTO.getInformationBoardId();
    }


    @PostMapping("/deleteProc")
    public String deleteProc(InformationBoardCommentDTO informationBoardCommentDTO) {
        return "redirect:/informationBoard/view/" + informationBoardCommentDTO.getInformationBoardId();
    }



}
