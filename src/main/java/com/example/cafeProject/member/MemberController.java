package com.example.cafeProject.member;

import com.example.cafeProject.security.ManagementOnly;
import com.example.cafeProject.validation.ValidationGroups;
import com.example.exception.EntityNotFoundException;
import com.example.exception.WrongPasswordException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;
    private final String basePath = "member";

    // view 결과에서 EntityNotFoundException이 발생하지 않으면 기존 링크를 반환, 아니라면 getNotFoundRedirectPath로 redirect
    private String loadPathOrRedirect(int id, Model model, String path) {
        try {
            Member entity = memberService.view(id);
            model.addAttribute("data", entity);
            return path;
        } catch (EntityNotFoundException e) {
            log.warn("대상이 되는 Entity를 찾을 수 없음. id={}", id, e);
            return getNotFoundRedirectPath();
        }
    }
    private String loadPathOrRedirect(Authentication authentication, Model model, String path) {
        try {
            Member entity = memberService.viewCurrentMember(authentication);
            model.addAttribute("data", entity);
            return path;
        } catch (EntityNotFoundException e) {
            log.warn("대상이 되는 Entity를 찾을 수 없음. principal={}", authentication != null ? authentication.getName() : "null", e);
            return getNotFoundRedirectPath();
        }
    }
    private String getNotFoundRedirectPath() {
        return "redirect:/";
    }

    // 공통 검증 실패 로그 + 뷰 이름 반환
    private String logValidationErrors(String action, String path, BindingResult bindingResult) {
        log.warn("[{} 검증 실패]", action);
        bindingResult.getFieldErrors().forEach(error ->
                log.warn("[{} 검증 실패 상세] - field: {}, value: {}, message: {}",
                        action,
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage())
        );
        return basePath + "/" + path;
    }

    @ManagementOnly
    @GetMapping("/list")
    public String list(
            Model model,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<Member> list = memberService.list(pageable);
        model.addAttribute("list", list);
        return basePath + "/list"; // ex) "memo/list"
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/view")
    public String view(
            Model model,
            Authentication authentication
    ) {
        return loadPathOrRedirect(authentication, model, basePath+"/view");
    }

    @ManagementOnly
    @GetMapping("/view/{id}")
    public String view(
            Model model,
            @PathVariable int id
    ) {
        return loadPathOrRedirect(id, model, basePath+"/view");
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("data", new MemberDTO());
        return basePath + "/create";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/passwordUpdate")
    public String passwordUpdate(Model model) {
        model.addAttribute("data", new PasswordChangeDTO());
        return "member/passwordUpdate";
    }

    @ManagementOnly
    @GetMapping("/roleUpdate/{id}")
    public String roleUpdate(
            @PathVariable Integer id,
            Model model
    ) {
        Member member = memberService.view(id);

        MemberDTO dto = MemberDTO.builder()
                .id(member.getId())
                .role(member.getRole())
                .build();

        model.addAttribute("data", dto);
        model.addAttribute("member", member);
        model.addAttribute("roles", RoleType.values());

        return "member/roleUpdate";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete")
    public String delete(
            Model model,
            Authentication authentication
    ) {
        model.addAttribute("data", new MemberDeleteDTO());
        return "member/delete";
    }

    @PostMapping("/createProc")
    public String createProc(
            @Validated(ValidationGroups.OnCreate.class) @ModelAttribute("data") MemberDTO dto,
            BindingResult bindingResult
    ) {
        dto.normalize();
        if (bindingResult.hasErrors()) return logValidationErrors("createProc", "create", bindingResult);

        try {
            memberService.setInsert(dto);
            return "redirect:/" + basePath + "/login";
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage());
            if (e.getMessage().contains("id")) {
                bindingResult.rejectValue("username", "duplicate", e.getMessage());
            } else if (e.getMessage().contains("email")) {
                bindingResult.rejectValue("email", "duplicate", e.getMessage());
            } else {
                bindingResult.reject("joinError", e.getMessage());
            }
            return basePath + "/create";
        } catch (Exception e) {
            log.error("[createProc] 예상하지 못 한 오류 : {}", e.getMessage());
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/passwordProc")
    public String passwordProc(
            @Valid @ModelAttribute("data") PasswordChangeDTO passwordChangeDTO,
            BindingResult bindingResult,
            Authentication authentication
    ) {
        passwordChangeDTO.normalize();
        if (bindingResult.hasErrors()) return logValidationErrors("passwordProc", "passwordUpdate", bindingResult);
        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
            log.warn("[비밀번호 변경 검증 실패] 새 비밀번호 불일치");
            bindingResult.rejectValue("confirmPassword", "mismatch", "비밀번호 확인이 일치하지 않습니다.");
            return "member/passwordUpdate";
        }

        try {
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_BANNED"))) {
                log.warn("[비밀번호 변경 차단] BANNED 계정, principal={}", authentication.getName());
                return "redirect:/";
            }
            memberService.setUpdate(authentication, passwordChangeDTO);
            return "redirect:/";
        } catch (EntityNotFoundException e) {
            log.warn("대상이 되는 Entity를 찾을 수 없음. principal={}", authentication.getName(), e);
            return getNotFoundRedirectPath();
        } catch (WrongPasswordException e) {
            log.warn("[비밀번호 변경 실패] 현재 비밀번호 불일치, username={}", authentication.getName());
            bindingResult.rejectValue("currentPassword", "wrong", e.getMessage());
            return "member/passwordUpdate";
        } catch (Exception e) {
            log.error("[passwordProc] 예상하지 못한 오류 : {}", e.getMessage());
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @ManagementOnly
    @PostMapping("/roleProc")
    public String roleProc(
            @Validated(ValidationGroups.OnUpdate.class) @ModelAttribute("data") MemberDTO dto,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        if (bindingResult.hasErrors()) return logValidationErrors("roleProc", "roleUpdate", bindingResult);

        try {
            Member admin = memberService.viewCurrentMember(authentication);
            memberService.updateRoleType(dto, admin);
            return "redirect:/member/view/" + dto.getId();
        } catch (EntityNotFoundException e) {
            log.warn("[roleProc] 대상이 되는 Entity를 찾을 수 없음. principal={}", authentication.getName(), e);
            return getNotFoundRedirectPath();
        } catch (IllegalArgumentException e) {
            log.warn("[roleProc] 권한 변경 실패. {}", e.getMessage());
            model.addAttribute("member", memberService.view(dto.getId()));
            model.addAttribute("roles", RoleType.values());
            bindingResult.reject("roleChangeError", e.getMessage());
            return "member/roleUpdate";
        } catch (Exception e) {
            log.error("[roleProc] 예상하지 못한 오류 : {}", e.getMessage());
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/deleteProc")
    public String deleteProc(
            @Valid @ModelAttribute("data") MemberDeleteDTO dto,
            BindingResult bindingResult,
            Authentication authentication
    ) {
        dto.normalize();
        if (bindingResult.hasErrors()) return logValidationErrors("deleteProc", "delete", bindingResult);

        try {
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_BANNED"))) {
                log.warn("[회원 탈퇴 차단] BANNED 계정, principal={}", authentication.getName());
                return "redirect:/";
            }
            memberService.setDelete(authentication, dto);
            return "redirect:/";
        } catch (EntityNotFoundException e) {
            log.warn("[deleteProc] 대상이 되는 Entity를 찾을 수 없음. principal={}", authentication.getName(), e);
            return getNotFoundRedirectPath();
        } catch (Exception e) {
            log.error("[deleteProc] 예상하지 못한 오류 : {}", e.getMessage());
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @GetMapping("/login")
    public String login(
            @ModelAttribute("data") MemberDTO dto
    ) {
        return "member/login";
    }

    @GetMapping("/login/error")
    public String loginError(
            @ModelAttribute("data") MemberDTO dto,
            Model model
    ) {
        model.addAttribute("loginError", true);
        return "member/login";
    }
}
