package com.example.cafeProject._boardTest;

import com.example.base.BaseImageController;
import com.example.cafeProject.member.RoleType;
import com.example.cafeProject.validation.AdminOnly;
import com.example.cafeProject.validation.ValidationGroups;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AdminOnly
@RequestMapping("/boardManagement")
public class BoardAdminController extends BaseImageController<Board, BoardDTO> {

    public BoardAdminController(BoardService service) {
        super(service, "boardManagement");
    }

    @ModelAttribute("roles")
    public RoleType[] roles() {
        return new RoleType[]{RoleType.GUEST, RoleType.USER, RoleType.MANAGER, RoleType.ADMIN};
    }

    @Override
    public String createProc(
            @Validated(ValidationGroups.OnCreate.class) @ModelAttribute("data") BoardDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("생성", "create", bindingResult);

        try {
            service.setInsert(dto);
            return "redirect:/" + basePath + "/list";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("readRole", "invalid", e.getMessage());
            return basePath + "/create";
        }
    }
}
