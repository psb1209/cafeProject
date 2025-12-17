package com.example.cafeProject.boardTest;

import com.example.base.BaseImageController;
import com.example.cafeProject.validation.AdminOnly;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AdminOnly
@RequestMapping("/boardManagement")
public class BoardAdminController extends BaseImageController<Board, BoardDTO> {

    public BoardAdminController(BoardService service) {
        super(service, "/boardManagement");
    }
}
