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

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final BaseCrudService<E, D> service;
    protected final String basePath;

    protected BaseCrudController(BaseCrudService<E, D> service, String basePath) {
        this.service = service;
        this.basePath = basePath;
    }

    // view 결과에서 EntityNotFoundException이 발생하지 않으면 기존 링크를 반환, 아니라면 getNotFoundRedirectPath로 redirect
    protected String loadPathOrRedirect(int id, Model model, String path) {
        try {
            E entity = service.view(id);
            model.addAttribute("data", entity);
            return path;
        } catch (EntityNotFoundException e) {
            log.warn("대상이 되는 Entity를 찾을 수 없음. id={}", id, e);
            return getNotFoundRedirectPath();
        }
    }
    // 해당 메서드를 Override해서 view 결과가 존재하지 않을 경우 보낼 링크를 커스터마이징할 수 있습니다.
    protected String getNotFoundRedirectPath() {
        // 기본값: 메인
        return "redirect:/";
    }

    // 공통 검증 실패 로그 + 뷰 이름 반환
    protected String logValidationErrors(String action, String path, BindingResult bindingResult) {
        log.warn("[{} 검증 실패]", action);
        bindingResult.getFieldErrors().forEach(error ->
                log.warn("[{} 검증 실패 상세] - field: {}, value: {}, message: {}",
                        action,
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage())
        );
        return action + "/" + path;
    }

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

    @GetMapping("/view/{id}")
    public String view(
            Model model,
            @PathVariable int id
    ) {
        return loadPathOrRedirect(id, model, basePath+"/view");
    }

    @GetMapping("/create")
    public String create() {
        return basePath + "/create";
    }

    @GetMapping("/update/{id}")
    public String update(
            Model model,
            @PathVariable int id
    ) {
        return loadPathOrRedirect(id, model, basePath+"/update");
    }

    @GetMapping("/delete/{id}")
    public String delete(
            Model model,
            @PathVariable int id
    ) {
        return loadPathOrRedirect(id, model, basePath+"/delete");
    }

    @PostMapping("/createProc")
    public String createProc(
            @Valid @ModelAttribute("data") D dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("생성", basePath + "/create", bindingResult);
        service.setInsert(dto);
        return "redirect:/" + basePath + "/list";
    }

    @PostMapping("/updateProc")
    public String updateProc(
            @Valid @ModelAttribute("data") D dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("수정", basePath + "/update", bindingResult);
        service.setUpdate(dto);
        return "redirect:/" + basePath + "/view/" + service.getIdFromDTO(dto);
    }

    @PostMapping("/deleteProc")
    public String deleteProc(
            @Valid @ModelAttribute("data") D dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("삭제", basePath + "/delete", bindingResult);
        service.setDelete(dto);
        return "redirect:/" + basePath + "/list";
    }
}
