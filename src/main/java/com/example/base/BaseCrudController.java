package com.example.base;

import com.example.exception.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

public abstract class BaseCrudController<E, D> {
    protected final BaseCrudService<E, D> service; // CRUD 비즈니스 로직을 담당하는 서비스
    protected final String basePath; // 기본으로 사용할 링크

    // 로그 찍는 용도 그 이상도 그 이하도 아님. log.***은 전부 무시해도 됨!!
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected BaseCrudController(BaseCrudService<E, D> service, String basePath) {
        this.service = service;
        this.basePath = basePath;
    }


    /**
     * 모든 POST 검증 실패 시 공통으로 사용하는 로깅 + 뷰 반환 메서드.
     * action: 로그에 찍힐 작업 이름, viewName: 실패 시 다시 보여줄 뷰 경로.
     */
    protected String logValidationErrors(String action, String viewName, BindingResult bindingResult) {
        log.warn("[{} 검증 실패]", action);
        bindingResult.getFieldErrors().forEach(error ->
                log.warn("[{} 검증 실패 상세] - field: {}, value: {}, message: {}",
                        action,
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage())
        );  // 모든 검증 오류를 log.warn으로 출력
        return basePath + "/" + viewName;
    }

    /**
     * id로 Member를 조회해서 존재하면 model에 "data"로 담고 지정된 뷰로 이동.
     * 대상이 없으면 경고 로그를 남기고 메인으로 redirect.
     */
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
     * 해당 메서드를 Override해서 view 결과가 존재하지 않을 경우 보낼 링크를 커스터마이징할 수 있습니다.
     */
    protected String getNotFoundRedirectPath() {
        // 기본값: 메인
        return "redirect:/";
    }

    /** 목록 화면 */
    @GetMapping("/list")
    public String list(
            Model model,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<E> list = service.list(pageable);
        model.addAttribute("list", list);
        return basePath + "/list"; // ex) "memo/list"
    }

    /** 상세 화면 */
    @GetMapping("/view/{id}")
    public String view(
            Model model,
            @PathVariable int id
    ) {
        return loadPathOrRedirect(id, model, basePath+"/view");
    }

    /** 등록 폼 화면 */
    @GetMapping("/create")
    public String create() {
        return basePath + "/create";
    }

    /** 수정 폼 화면 */
    @GetMapping("/update/{id}")
    public String update(
            Model model,
            @PathVariable int id
    ) {
        return loadPathOrRedirect(id, model, basePath+"/update");
    }

    /** 삭제 확인 화면 */
    @GetMapping("/delete/{id}")
    public String delete(
            Model model,
            @PathVariable int id
    ) {
        return loadPathOrRedirect(id, model, basePath+"/delete");
    }

    /**
     * 등록 처리
     * - @Valid로 DTO 검증
     * - 실패 시: 다시 create 폼으로
     * - 성공 시: 목록으로 redirect
     */
    @PostMapping("/createProc")
    public String createProc(
            @Valid @ModelAttribute("data") D dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("생성", "create", bindingResult);
        service.setInsert(dto);
        return "redirect:/" + basePath + "/list";
    }

    /**
     * 수정 처리
     * - @Valid로 DTO 검증
     * - 실패 시: update 폼으로
     * - 성공 시: 상세보기로 redirect
     */
    @PostMapping("/updateProc")
    public String updateProc(
            @Valid @ModelAttribute("data") D dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("수정", "update", bindingResult);
        service.setUpdate(dto);
        return "redirect:/" + basePath + "/view/" + service.getIdFromDTO(dto);
    }

    /**
     * 삭제 처리
     * - @Valid로 DTO 검증
     * - 실패 시: delete 확인 화면으로
     * - 성공 시: 목록으로 redirect
     */
    @PostMapping("/deleteProc")
    public String deleteProc(
            @Valid @ModelAttribute("data") D dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("삭제", "delete", bindingResult);
        service.setDelete(dto);
        return "redirect:/" + basePath + "/list";
    }
}
