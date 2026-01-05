package com.example.cafeProject._boardTest;

import com.example.base.BaseImageController;
import com.example.cafeProject.member.RoleType;
import com.example.cafeProject.validation.AdminOnly;
import com.example.cafeProject.validation.ValidationGroups;
import com.example.exception.DuplicateValueException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AdminOnly
@RequestMapping("/boardManagement")
public class BoardAdminController extends BaseImageController<Board, BoardDTO> {

    private final BoardService boardService;

    public BoardAdminController(BoardService service) {
        super(service, "boardManagement");
        this.boardService = service;
    }

    @ModelAttribute("writeRoles")
    public RoleType[] writeRoles() {
        return new RoleType[]{RoleType.USER, RoleType.MANAGER, RoleType.ADMIN};
    }

    @ModelAttribute("readRoles")
    public RoleType[] readRoles() {
        return new RoleType[]{RoleType.GUEST, RoleType.USER, RoleType.MANAGER, RoleType.ADMIN};
    }

    @Override
    public String createProc(
            @Validated(ValidationGroups.OnCreate.class) @ModelAttribute("data") BoardDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("생성", "create", bindingResult);

        try {
            boardService.setInsert(dto);
            return "redirect:/" + basePath + "/list";
        } catch (DuplicateValueException e) {
            bindingResult.rejectValue(e.getField(), "duplicate", e.getMessage());
            return basePath + "/create";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("readRole", "invalid", e.getMessage());
            return basePath + "/create";
        } catch (AccessDeniedException e) {
            return "redirect:/member/login";
        } catch (Exception e) {
            log.error("[createProc] 예상하지 못 한 오류 : {}", e.getMessage(), e);
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @Override
    public String updateProc(
            @Validated(ValidationGroups.OnUpdate.class) @ModelAttribute("data") BoardDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("수정", "update", bindingResult);
        try {
            boardService.setUpdate(dto);
            return "redirect:/" + basePath + "/view/" + boardService.getIdFromDTO(dto);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("readRole", "invalid", e.getMessage());
            return basePath + "/update";
        } catch (Exception e) {
            log.error("[updateProc] 예상하지 못 한 오류 : {}", e.getMessage(), e);
            return "redirect:/error/runtimeErrorPage";
        }
    }
}
