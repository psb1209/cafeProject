package com.example.cafeProject._cafeTest;

import com.example.base.BaseImageController;
import com.example.cafeProject.communityBoard.CommunityBoardService;
import com.example.cafeProject.informationBoard.InformationBoardService;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoard.NoticeBoardService;
import com.example.cafeProject.validation.ValidationGroups;
import com.example.exception.DuplicateValueException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/cafe")
public class CafeController extends BaseImageController<Cafe, CafeDTO> {

    private final CafeService cafeService;
    private final MemberService memberService;
    private final NoticeBoardService noticeBoardService;
    private final CommunityBoardService communityBoardService;
    private final InformationBoardService informationBoardService;

    public CafeController(
            CafeService service,
            MemberService memberService,
            NoticeBoardService noticeBoardService,
            CommunityBoardService communityBoardService,
            InformationBoardService informationBoardService
    ) {
        super(service, "cafe");
        this.cafeService = service;
        this.memberService = memberService;
        this.noticeBoardService = noticeBoardService;
        this.communityBoardService = communityBoardService;
        this.informationBoardService = informationBoardService;
    }

    @Override
    public String list(
            Model model,
            Pageable pageable
    ) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/cafeList")
    public String cafeList(
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            Authentication authentication,
            Model model
    ) {
        Page<CafeDTO> list = cafeService.listVisibleDTO(pageable, keyword);
        model.addAttribute("list", list);
        model.addAttribute("keyword", keyword);
        return super.basePath + "/list"; // ex) "memo/list"
    }

    // "/cafe/{code} 형식으로 오는 링크를 mainPage로 떠넘김
    @GetMapping("/{code}")
    public String enter(@PathVariable String code) {
        return "redirect:/cafe/main?c=" + code;
    }

    @GetMapping("/main")
    public String mainPage(
            @PageableDefault(size=5, sort="id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String code,
            Model model
    ) {
        model.addAttribute("code", code);
        model.addAttribute("noticeBoardList", noticeBoardService.list(pageable, null));
        model.addAttribute("communityBoardList", communityBoardService.list(pageable,null));
        model.addAttribute("informationBoardList", informationBoardService.getSelectAllPage(pageable, null));
        model.addAttribute("activeMenu", "main");
        return super.basePath + "/main"; // ex) "memo/list"
    }

    @Override
    public String createProc(
            @Validated(ValidationGroups.OnCreate.class) @ModelAttribute("data") CafeDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("생성", "create", bindingResult);

        try {
            cafeService.setInsert(dto);
            return "redirect:/" + super.basePath + "/cafeList";
        } catch (DuplicateValueException e) {
            bindingResult.rejectValue(e.getField(), "duplicate", e.getMessage());
            return super.basePath + "/create";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("readRole", "invalid", e.getMessage());
            return super.basePath + "/create";
        } catch (AccessDeniedException e) {
            return "redirect:/member/login";
        } catch (Exception e) {
            log.error("[createProc] 예상하지 못 한 오류 : {}", e.getMessage(), e);
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @Override
    public String updateProc(
            @Validated(ValidationGroups.OnUpdate.class) @ModelAttribute("data") CafeDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("수정", "update", bindingResult);
        try {
            cafeService.setUpdate(dto);
            return "redirect:/" + super.basePath + "/view/" + cafeService.getIdFromDTO(dto);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("readRole", "invalid", e.getMessage());
            return super.basePath + "/update";
        } catch (Exception e) {
            log.error("[updateProc] 예상하지 못 한 오류 : {}", e.getMessage(), e);
            return "redirect:/error/runtimeErrorPage";
        }
    }
}
