package com.example.cafeProject._boardTest;

import com.example.base.BaseImageController;
import com.example.cafeProject._cafeTest.CafeDTO;
import com.example.cafeProject._cafeTest.CafeService;
import com.example.cafeProject.member.RoleType;
import com.example.validation.ManagementOnly;
import com.example.validation.ValidationGroups;
import com.example.exception.DuplicateValueException;
import com.example.exception.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@ManagementOnly
@RequestMapping("/boardManagement")
public class BoardAdminController extends BaseImageController<Board, BoardDTO> {

    private final BoardService boardService;
    private final CafeService cafeService;
    private final HttpServletRequest request;

    public BoardAdminController(
            BoardService service,
            CafeService cafeService,
            HttpServletRequest request
    ) {
        super(service, "boardManagement");
        this.boardService = service;
        this.cafeService = cafeService;
        this.request = request;
    }

    /** 템플릿에서 공통으로 쓰기 위해 모델에 cafeCode 주입 */
    @ModelAttribute("cafe")
    public CafeDTO cafe(
            @RequestParam(name = "c", required = false) String c
    ) {
        if (c == null || c.isBlank()) return null; // c 없이 들어오는 페이지는 일단 null
        try {
            return cafeService.viewVisibleDTOByCode(c);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
    @ModelAttribute("cafeCode")
    public String cafeCode(@RequestParam(name = "c", required = false) String cafeCode) {
        return cafeCode;
    }

    /** 각 메서드들의 c 필수화 */
    private String requireCafeCode() {
        String c = request.getParameter("c");
        if (c == null || c.isBlank()) {
            log.warn("[Missing c] method={} uri={} query={} referer={} paramKeys={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getQueryString(),
                    request.getHeader("Referer"),
                    request.getParameterMap().keySet()
            );
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "cafeCode가 없습니다.");
        }
        return c;
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
    public String list(
            Model model,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        model.addAttribute("activeMenu", "boardManagement");
        model.addAttribute("list", boardService.listDTO(requireCafeCode(), pageable));
        return super.isAjaxRequest()
                ? basePath + "/list :: boardListArea"
                : basePath + "/list";
    }

    @Override
    public String create(Model model) {
        requireCafeCode();
        return super.create(model);
    }

    @Override
    public String update(
            Model model,
            @PathVariable int id
    ) {
        requireCafeCode();
        return super.update(model, id);
    }

    @Override
    public String delete(Model model, @PathVariable int id) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Override
    public String createProc(
            @Validated(ValidationGroups.OnCreate.class) @ModelAttribute("data") BoardDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("생성", "create", bindingResult);
        dto.setCafeCode(requireCafeCode()); // 변조 방지

        try {
            boardService.setInsert(dto);
            return "redirect:/" + basePath + "/list?c=" + dto.getCafeCode();
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
        dto.setCafeCode(requireCafeCode()); // 변조 방지

        try {
            log.info("before setUpdate cafeCode={}", dto.getCafeCode());
            boardService.setUpdate(dto);
            log.info("after  setUpdate cafeCode={}", dto.getCafeCode());
            return "redirect:/" + basePath + "/view/" + dto.getId() + "?c=" + dto.getCafeCode();
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("readRole", "invalid", e.getMessage());
            return basePath + "/update";
        } catch (Exception e) {
            log.error("[updateProc] 예상하지 못 한 오류 : {}", e.getMessage(), e);
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @Override
    public String deleteProc(
            @Validated(ValidationGroups.OnDelete.class) @ModelAttribute("data") BoardDTO dto,
            BindingResult bindingResult
    ) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @Override
    protected String getNotFoundRedirectPath() {
        String c = request.getParameter("c");
        if (c == null || c.isBlank()) return "redirect:/";
        return "redirect:/" + basePath + "/list?c=" + c;
    }
}
