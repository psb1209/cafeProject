package com.example.cafeProject._postTest;

import com.example.cafeProject._boardTest.BoardDTO;
import com.example.cafeProject._boardTest.BoardService;
import com.example.cafeProject._cafeTest.CafeDTO;
import com.example.cafeProject._cafeTest.CafeService;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.validation.AdminOnly;
import com.example.cafeProject.validation.ManagementOnly;
import com.example.cafeProject.validation.ValidationGroups;
import com.example.exception.EntityNotFoundException;
import com.example.exception.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
@RequestMapping("/cafe/{cafeCode}/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final BoardService boardService;
    private final CafeService cafeService;
    private final MemberService memberService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${app.image.upload-dir}")
    protected String uploadDir; //저장할 폴더

    @Value("${app.image.url-prefix}")
    protected String urlPrefix; // 클라이언트에 반환할 URL

    /**
     * 모든 핸들러 메서드 실행 전에 모델에 "imageUrlPrefix"를 자동 주입
     * - 뷰(Thymeleaf)에서 이미지 경로를 만들 때 공통으로 사용
     * - 설정값이 / 로 끝나지 않으면 /를 붙여서 보정
     */
    @ModelAttribute("imageUrlPrefix")
    public String imageUrlPrefix() {
        // /attach/summernote/ 형태로 끝에 / 보정
        return urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
    }

    /**
     * 모든 핸들러 메서드 실행 전에 모델에 "board"를 자동 주입
     * - 요청 파라미터 b(=board code)가 있으면 해당 게시판 DTO를 조회해서 모델에 담음
     * - b 없이 들어오는 요청(예: /post/view/1 같이 b를 안 붙인 경우)은 null 반환
     * 뷰에서 `${board}`를 바로 참조할 수 있게 만들기 위한 전역 모델 세팅용 훅
     */
    @ModelAttribute("board")
    public BoardDTO boardAttr(
            @PathVariable String cafeCode,
            @RequestParam(name = "b", required = false) String boardCode
    ) {
        if (boardCode == null || boardCode.isBlank()) return null; // b 없이 들어오는 페이지는 일단 null
        return boardService.viewDTOByCode(cafeCode, boardCode);
    }

    @ModelAttribute("cafe")
    public CafeDTO cafeAttr(
            @PathVariable String cafeCode
    ) {
        if (cafeCode == null || cafeCode.isBlank()) return null;
        return cafeService.viewDTOByCode(cafeCode);
    }

    /** dto에 값을 담을 때 해당 필드는 무시됨 */
    protected String[] disallowedFields() {
        return new String[]{"memberId", "username", "createDate", "deleted", "deletedAt", "titleKey", "boardId", "boardCode"};
    }

    @InitBinder("data")
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields(disallowedFields());
    }

    @ManagementOnly
    @PostMapping("/toggleNotice/{id}")
    public String toggleNotice(
            @PathVariable String cafeCode,
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        postService.toggleNotice(id);
        redirectAttributes.addFlashAttribute("msg", "공지 상태가 변경되었습니다.");
        return "redirect:/cafe/" + cafeCode + "/post/list?b=" + postService.viewDTO(id).getBoardCode();
    }

    @GetMapping("/list")
    public String list(
            @PathVariable String cafeCode,
            @RequestParam(name = "b") String boardCode,
            @ModelAttribute("board") BoardDTO board,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        try {
            Integer boardId = board.getId();

            model.addAttribute("noticeList", postService.noticeListByBoardIdDTO(boardId));
            model.addAttribute("list", postService.listByBoardIdDTO(boardId, keyword, pageable));
            model.addAttribute("keyword", keyword);
            return "post/list";
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/view/{id}")
    public String view(
            @PathVariable String cafeCode,
            @RequestParam(name="b", required = false) String boardCode,
            @PathVariable int id,
            Authentication authentication,
            Model model
    ) {
        PostDTO dto = postService.viewDetailDTO(id);

        // b가 없으면 같은 cafeCode 경로로 보정 리다이렉트
        if (boardCode == null || boardCode.isBlank()) {
            return "redirect:/cafe/" + cafeCode + "/post/view/" + id + "?b=" + dto.getBoardCode();
        }

        model.addAttribute("data", dto);
        model.addAttribute("canEdit", postService.canEdit(id, authentication));
        return "post/view";
    }

    @ManagementOnly
    @GetMapping("/trashList")
    public String trashList(
            @PathVariable String cafeCode,
            @RequestParam(name="b") String boardCode,
            @ModelAttribute("board") BoardDTO board,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        try {
            model.addAttribute("list", postService.trashListByBoardIdDTO(board.getId(), pageable));
            model.addAttribute("trash", true);
            return "post/list";
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @AdminOnly
    @GetMapping("/trash/{id}")
    public String trashView(
            @PathVariable String cafeCode,
            @RequestParam(name="b", required = false) String boardCode,
            @PathVariable int id,
            Authentication authentication,
            Model model
    ) {
        PostDTO dto = postService.viewTrashDTO(id);
        if (boardCode == null || boardCode.isBlank()) return "redirect:/cafe/" + cafeCode + "/post/trash/" + id + "?b=" + dto.getBoardCode();

        model.addAttribute("data", dto);
        model.addAttribute("isTrash", true);
        model.addAttribute("canEdit", false);
        return "post/view";
    }

    @GetMapping("/create")
    public String create(
            @PathVariable String cafeCode,
            @RequestParam(name="b") String boardCode,
            @ModelAttribute("board") BoardDTO board,
            Model model
    ) {
        try {
            PostDTO dto = postService.newDTO();

            // 변조 방지
            dto.setBoardId(board.getId());
            dto.setBoardCode(board.getCode());

            model.addAttribute("data", dto);
            return "post/create";
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/update/{id}")
    public String update(
            @PathVariable String cafeCode,
            @RequestParam(name="b", required = false) String boardCode,
            @PathVariable int id,
            Model model
    ) {
        PostDTO dto = postService.viewDetailDTO(id);
        if (boardCode == null || boardCode.isBlank())
            return "redirect:/cafe/" + cafeCode + "/post/update/" + id + "?b=" + dto.getBoardCode();

        model.addAttribute("data", dto);
        return "post/update";
    }

    @PostMapping("/createProc")
    public String createProc(
            @PathVariable String cafeCode,
            @RequestParam(name="b") String boardCode,
            @ModelAttribute("board") BoardDTO board,
            @Validated(ValidationGroups.OnCreate.class) @ModelAttribute("data") PostDTO dto,
            RedirectAttributes redirectAttributes,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return "post/create";
        try {
            // 변조 방지
            dto.setBoardId(board.getId());
            dto.setBoardCode(board.getCode());

            postService.setInsert(dto);
            boolean isUpgraded = postService.gradeUpdateCheck(dto);
            if(isUpgraded)  //등급 상승시 "msg" 데이터를 같이 리다이렉트 시킴
                redirectAttributes.addFlashAttribute("msg","축하합니다! 등급이 올랐습니다!🎉");
            return "redirect:/cafe/" + cafeCode + "/post/list?b=" + dto.getBoardCode();
        } catch (AccessDeniedException | PermissionDeniedException e) {
            bindingResult.reject("permissionDenied", e.getMessage());
            return "post/create";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("invalid", e.getMessage());
            return "post/create";
        } catch (Exception e) {
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @PostMapping("/updateProc")
    public String updateProc(
            @PathVariable String cafeCode,
            @RequestParam(name="b") String boardCode,
            @ModelAttribute("board") BoardDTO board,
            @Validated(ValidationGroups.OnUpdate.class) @ModelAttribute("data") PostDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return "post/update";
        try {
            // 변조 방지
            dto.setBoardId(board.getId());
            dto.setBoardCode(board.getCode());

            postService.setUpdate(dto);
            return "redirect:/cafe/" + cafeCode + "/post/view/" + dto.getId() + "?b=" + dto.getBoardCode();
        } catch (AccessDeniedException | PermissionDeniedException e) {
            bindingResult.reject("permissionDenied", e.getMessage());
            return "post/update";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("invalid", e.getMessage());
            return "post/update";
        } catch (Exception e) {
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @PostMapping("/deleteProc/{id}")
    public String deleteProc(
            @PathVariable String cafeCode,
            @PathVariable int id,
            @RequestParam(name="b") String boardCode,
            RedirectAttributes redirectAttributes
    ) {
        try {
            PostDTO dto = postService.newDTO();
            dto.setId(id);
            postService.softDelete(dto); // 소프트 삭제로 동작
            return "redirect:/cafe/" + cafeCode + "/post/list?b=" + boardCode;
        } catch (AccessDeniedException | PermissionDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cafe/" + cafeCode + "/post/view/" + id + "?b=" + boardCode;
        }
    }
}
