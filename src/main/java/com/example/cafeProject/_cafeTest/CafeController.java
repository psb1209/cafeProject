package com.example.cafeProject._cafeTest;

import com.example.base.BaseImageController;
import com.example.cafeProject._boardTest.BoardDTO;
import com.example.cafeProject._boardTest.BoardService;
import com.example.cafeProject._boardTest.DefaultBoard;
import com.example.cafeProject._postTest.PostDTO;
import com.example.cafeProject._postTest.PostService;
import com.example.cafeProject.communityBoard.CommunityBoardService;
import com.example.cafeProject.informationBoard.InformationBoardService;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoard.NoticeBoardService;
import com.example.cafeProject.validation.ManagementOnly;
import com.example.cafeProject.validation.ValidationGroups;
import com.example.exception.DuplicateValueException;
import com.example.exception.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

import java.util.List;

@Controller
@RequestMapping("/cafe")
public class CafeController extends BaseImageController<Cafe, CafeDTO> {

    private final CafeService cafeService;
    private final BoardService boardService;
    private final PostService postService;

    public CafeController(
            CafeService service,
            BoardService boardService,
            PostService postService
    ) {
        super(service, "cafe");
        this.cafeService = service;
        this.boardService = boardService;
        this.postService = postService;
    }

    /**
     * 모든 핸들러 메서드 실행 전에 모델에 "cafe"를 자동 주입
     * - 요청 파라미터 c(=cafe code)가 있으면 해당 게시판 DTO를 조회해서 모델에 담음
     * - c 없이 들어오는 요청(예: /post/view/1 같이 c를 안 붙인 경우)은 null 반환
     * 뷰에서 `${cafe}`를 바로 참조할 수 있게 만들기 위한 전역 모델 세팅용 훅
     */
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
        Page<CafeDTO> list = cafeService.listVisibleDTO(pageable, keyword, authentication);
        model.addAttribute("list", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeMenu", "cafeList");
        return super.basePath + "/list";
    }

    // "/cafe/{code} 형식으로 오는 링크를 mainPage로 떠넘김
    @GetMapping("/{code}")
    public String enter(@PathVariable String code) {
        return "redirect:/cafe/main?c=" + code;
    }

    @GetMapping("/main")
    public String mainPage(
            @PageableDefault(size = 5, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "c", required = false) String code,
            Model model
    ) {
        model.addAttribute("code", code);
        model.addAttribute("noticeBoardList", latestPosts(code, DefaultBoard.NOTICE.getCode(), pageable));
        model.addAttribute("informationBoardList", latestPosts(code, DefaultBoard.INFORMATION.getCode(), pageable));
        model.addAttribute("communityBoardList", latestPosts(code, DefaultBoard.COMMUNITY.getCode(), pageable));
        model.addAttribute("activeMenu", "main");
        return super.basePath + "/main"; // ex) "memo/list"
    }

    /** 소개 페이지 */
    @GetMapping("/introduction")
    public String introduction(
            @RequestParam(name = "c") String cafeCode,
            Model model
    ) {
        model.addAttribute("cafeCode", cafeCode);
        model.addAttribute("activeMenu", "introduction");
        return super.basePath + "/introduction"; // "cafe/introduction"
    }


    /** 메타데이터 */
    @ManagementOnly
    @GetMapping("/meta/{id}")
    public String meta(
            Model model,
            @PathVariable int id
    ) {
        model.addAttribute("cafe", cafeService.viewDTO(id));
        return loadPathOrRedirect(id, model, basePath+"/meta");
    }

    @ManagementOnly
    public String create(Model model) {
        return super.create(model);
    }

    @ManagementOnly
    public String update(
            Model model,
            @PathVariable int id
    ) {
        return super.update(model, id);
    }

    @Override
    @ManagementOnly
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
    @ManagementOnly
    public String updateProc(
            @Validated(ValidationGroups.OnUpdate.class) @ModelAttribute("data") CafeDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("수정", "update", bindingResult);
        try {
            cafeService.setUpdate(dto);
            return "redirect:/" + super.basePath + "/meta/" + cafeService.getIdFromDTO(dto);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("readRole", "invalid", e.getMessage());
            return super.basePath + "/update";
        } catch (Exception e) {
            log.error("[updateProc] 예상하지 못 한 오류 : {}", e.getMessage(), e);
            return "redirect:/error/runtimeErrorPage";
        }
    }

    private List<PostDTO> latestPosts(String cafeCode, String boardCode, Pageable pageable) {
        try {
            BoardDTO board = boardService.viewVisibleDTOByCode(cafeCode, boardCode);
            return postService.listByBoardIdDTO(board.getId(), null, pageable).getContent();
        } catch (Exception e) {
            return List.of();
        }
    }
}
