package com.example.cafeProject.boardTest;

import com.example.base.BaseImageController;
import com.example.cafeProject.member.RoleType;
import com.example.cafeProject.validation.AdminOnly;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AdminOnly
@RequestMapping("/boardManagement")
public class BoardAdminController extends BaseImageController<Board, BoardDTO> {

    public BoardAdminController(BoardService service) {
        super(service, "/boardManagement");
    }

    @Override
    public String create(Model model) {
        model.addAttribute("data", new BoardDTO());
        model.addAttribute("roles", new RoleType[]{RoleType.USER, RoleType.MANAGER, RoleType.ADMIN});
        return super.create(model);
    }
}
