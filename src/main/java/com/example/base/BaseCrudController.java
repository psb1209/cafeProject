package com.example.base;

import com.example.validation.ValidationGroups;
import com.example.exception.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public abstract class BaseCrudController<E, D> {

    protected final BaseCrudService<E, D> service; // CRUD 비즈니스 로직을 담당하는 서비스
    protected final String basePath; // 기본으로 사용할 링크 + view 폴더명 (기존 호환)

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected BaseCrudController(BaseCrudService<E, D> service, String basePath) {
        this.service = service;
        this.basePath = normalizeBasePath(basePath);
    }

    /** dto에 값을 담을 때 해당 필드는 무시됨 */
    protected String[] disallowedFields() {
        return new String[]{"memberId", "username", "createDate"};
    }

    @InitBinder("data")
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields(disallowedFields());
    }

    /** 목록 화면 */
    @GetMapping({"", "/", "/list"})
    public String list(
            Model model,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<D> list = service.listDTO(pageable);
        model.addAttribute("list", list);
        return basePath + "/list";
    }

    /** 상세 화면 */
    @GetMapping("/view/{id}")
    public String view(Model model, @PathVariable int id) {
        return loadPathOrRedirect(id, model, basePath + "/view");
    }

    /** 등록 폼 화면 */
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("data", service.newDTO());
        return basePath + "/create";
    }

    /** 수정 폼 화면 */
    @GetMapping("/update/{id}")
    public String update(Model model, @PathVariable int id) {
        return loadPathOrRedirect(id, model, basePath + "/update");
    }

    /** 삭제 확인 화면 */
    @GetMapping("/delete/{id}")
    public String delete(Model model, @PathVariable int id) {
        return loadPathOrRedirect(id, model, basePath + "/delete");
    }

    /** 등록 처리 */
    @PostMapping("/createProc")
    public String createProc(
            @Validated(ValidationGroups.OnCreate.class) @ModelAttribute("data") D dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("생성", "create", bindingResult);
        service.setInsert(dto);
        return "redirect:/" + basePath + "/list";
    }

    /** 수정 처리 */
    @PostMapping("/updateProc")
    public String updateProc(
            @Validated(ValidationGroups.OnUpdate.class) @ModelAttribute("data") D dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("수정", "update", bindingResult);
        service.setUpdate(dto);
        return "redirect:/" + basePath + "/view/" + service.getIdFromDTO(dto);
    }

    /** 삭제 처리 */
    @PostMapping("/deleteProc")
    public String deleteProc(
            @Validated(ValidationGroups.OnDelete.class) @ModelAttribute("data") D dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("삭제", "delete", bindingResult);
        service.setDelete(dto);
        return "redirect:/" + basePath + "/list";
    }

    /** 검증 실패 공통 로깅 + 뷰 반환 */
    protected String logValidationErrors(String action, String viewName, BindingResult bindingResult) {
        log.warn("[{} 검증 실패]", action);
        bindingResult.getFieldErrors().forEach(error ->
                log.warn("[{} 검증 실패 상세] - field: {}, value: {}, message: {}",
                        action,
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage())
        );
        return basePath + "/" + viewName;
    }

    /** id로 조회해서 model에 담고 path로 이동, 없으면 redirect */
    protected String loadPathOrRedirect(int id, Model model, String path) {
        try {
            D dto = service.viewDTO(id);
            model.addAttribute("data", dto);
            return path;
        } catch (EntityNotFoundException e) {
            log.warn("대상이 되는 Entity를 찾을 수 없음. id={}", id, e);
            return getNotFoundRedirectPath();
        }
    }

    /**
     * view 결과가 없을 경우 보내는 redirect
     * 필요하면 override 해서 커스터마이징
     */
    protected String getNotFoundRedirectPath() {
        // 기본값: 메인
        return "redirect:/";
    }

    /** 지금의 요청이 Ajax 요청인지 판별하는 메서드 */
    protected boolean isAjaxRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return false;

        HttpServletRequest req = attrs.getRequest();
        String requestedWith = req.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith);
    }

    /** 들어오는 basePath 값을 정규화 */
    private static String normalizeBasePath(String raw) throws IllegalArgumentException {
        if (raw == null) throw new IllegalArgumentException("basePath is null");
        String s = raw.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        if (s.isBlank()) throw new IllegalArgumentException("basePath is blank: " + raw);
        return s;
    }
}