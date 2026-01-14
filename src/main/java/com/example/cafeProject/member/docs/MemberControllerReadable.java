package com.example.cafeProject.member.docs;

import com.example.cafeProject.member.*;
import com.example.validation.ManagementOnly;
import com.example.validation.ValidationGroups;
import com.example.exception.EntityNotFoundException;
import com.example.exception.PermissionDeniedException;
import com.example.exception.WrongPasswordException;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 읽기 전용(문서용) MemberController
 * - 실제 서비스 빈이 아님 (의도적으로 @Controller 없음)
 * - 로그 제거 버전: "로직만" 보기 위한 참고용 파일
 * 실제 동작은 MemberController 담당
 * 해당 파일은 2026년 1월 9일에 마지막으로 수정됨
 */
// @Controller
// @RequestMapping("/member")
// @RequiredArgsConstructor
@SuppressWarnings("unused")
@Deprecated(since = "2025-12-16", forRemoval = false)
public final class MemberControllerReadable {

    private final MemberService memberService;
    private final String basePath = "member";

    private MemberControllerReadable(MemberService memberService) {
        this.memberService = memberService;
        throw new UnsupportedOperationException("Docs-only class. Use MemberController instead.");
    }

    @ManagementOnly
    @GetMapping("/list")
    public String list(
            Model model,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication
    ) {
        Page<Member> list = memberService.list(pageable);
        model.addAttribute("list", list);
        return basePath + "/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/view")
    public String view(Model model, Authentication authentication) {
        return loadPathOrRedirect(authentication, model, basePath + "/view");
    }

    @ManagementOnly
    @GetMapping("/view/{id}")
    public String view(Model model, @PathVariable int id, Authentication authentication) {
        return loadPathOrRedirect(id, model, basePath + "/view");
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("data", new MemberDTO());
        return basePath + "/create";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/passwordUpdate")
    public String passwordUpdate(Model model, Authentication authentication) {
        model.addAttribute("data", new PasswordChangeDTO());
        return basePath + "/passwordUpdate";
    }

    @ManagementOnly
    @GetMapping("/roleUpdate/{id}")
    public String roleUpdate(
            @PathVariable Integer id,
            Authentication authentication,
            Model model
    ) {
        Member member = memberService.view(id);

        MemberDTO dto = MemberDTO.builder()
                .id(member.getId())
                .role(member.getRole())
                .build();

        RoleType[] roles = allowedRoleOptions(authentication);
        if (roles == null) return "redirect:/";

        model.addAttribute("data", dto);
        model.addAttribute("member", member);
        model.addAttribute("roles", roles);

        return basePath + "/roleUpdate";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete")
    public String delete(Model model, Authentication authentication) {
        model.addAttribute("data", new MemberDeleteDTO());
        return basePath + "/delete";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/createProc")
    public String createProc(
            @Validated(ValidationGroups.OnCreate.class) @ModelAttribute("data") MemberDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return validationFailView("create", bindingResult);

        try {
            memberService.setInsert(dto);
            return "redirect:/" + basePath + "/login";
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("id")) {
                bindingResult.rejectValue("username", "duplicate", e.getMessage());
            } else if (e.getMessage() != null && e.getMessage().contains("email")) {
                bindingResult.rejectValue("email", "duplicate", e.getMessage());
            } else {
                bindingResult.reject("joinError", e.getMessage());
            }
            return basePath + "/create";
        } catch (Exception e) {
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
        if (bindingResult.hasErrors()) return validationFailView("passwordUpdate", bindingResult);

        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "비밀번호 확인이 일치하지 않습니다.");
            return basePath + "/passwordUpdate";
        }

        try {
            if (memberService.isNotLogin(authentication)) return "redirect:/member/login";
            if (authentication.getAuthorities().toString().contains("ROLE_BANNED")) return "redirect:/";

            memberService.setUpdate(authentication, passwordChangeDTO);
            return "redirect:/";
        } catch (AccessDeniedException e) {
            return "redirect:/";
        } catch (EntityNotFoundException e) {
            return "redirect:/";
        } catch (WrongPasswordException e) {
            bindingResult.rejectValue("currentPassword", "wrong", e.getMessage());
            return basePath + "/passwordUpdate";
        } catch (Exception e) {
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
        if (bindingResult.hasErrors()) return validationFailView("roleUpdate", bindingResult);

        try {
            Member admin = memberService.viewCurrentMember(authentication);
            memberService.updateRoleType(dto, admin);
            return "redirect:/" + basePath + "/view/" + dto.getId();
        } catch (AccessDeniedException e) {
            return "redirect:/";
        } catch (EntityNotFoundException e) {
            return "redirect:/";
        } catch (IllegalArgumentException | PermissionDeniedException e) {
            RoleType[] roles = allowedRoleOptions(authentication);
            if (roles == null) return "redirect:/";

            model.addAttribute("member", memberService.view(dto.getId()));
            model.addAttribute("roles", roles);
            bindingResult.reject("roleChangeError", e.getMessage());
            return basePath + "/roleUpdate";
        } catch (Exception e) {
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/deleteProc")
    public String deleteProc(
            @Valid @ModelAttribute("data") MemberDeleteDTO dto,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) return validationFailView("delete", bindingResult);

        try {
            if (memberService.isNotLogin(authentication)) return "redirect:/member/login";
            if (authentication.getAuthorities().toString().contains("ROLE_BANNED")) return "redirect:/";

            memberService.setDelete(authentication, dto);

            // 소프트 삭제 후 현재 세션/인증을 정리
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();

            return "redirect:/";
        } catch (AccessDeniedException e) {
            return "redirect:/";
        } catch (EntityNotFoundException e) {
            return "redirect:/";
        } catch (WrongPasswordException e) {
            bindingResult.rejectValue("password", "wrong", e.getMessage());
            return basePath + "/delete";
        } catch (Exception e) {
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @GetMapping("/login")
    public String login(@ModelAttribute("data") MemberDTO dto) {
        return basePath + "/login";
    }

    @GetMapping("/login/error")
    public String loginError(@ModelAttribute("data") MemberDTO dto, Model model) {
        model.addAttribute("loginError", true);
        return basePath + "/login";
    }

    // ===== helpers (로깅 제거 버전) =====

    private String validationFailView(String path, BindingResult bindingResult) {
        // 로깅만 제거하고, 동작(뷰 복귀)은 유지
        return basePath + "/" + path;
    }

    private String loadPathOrRedirect(int id, Model model, String path) {
        try {
            Member entity = memberService.view(id);
            model.addAttribute("data", entity);
            return path;
        } catch (EntityNotFoundException e) {
            return "redirect:/";
        }
    }

    private String loadPathOrRedirect(Authentication authentication, Model model, String path) {
        try {
            Member entity = memberService.viewCurrentMember(authentication);
            model.addAttribute("data", entity);
            return path;
        } catch (AccessDeniedException e) {
            return "redirect:/";
        } catch (EntityNotFoundException e) {
            return "redirect:/";
        }
    }

    private RoleType[] allowedRoleOptions(Authentication authentication) {
        if (authentication.getAuthorities().toString().contains("ROLE_ADMIN"))
            return new RoleType[]{RoleType.BANNED, RoleType.USER, RoleType.MANAGER};

        if (authentication.getAuthorities().toString().contains("ROLE_MANAGER"))
            return new RoleType[]{RoleType.BANNED, RoleType.USER};

        return null;
    }
}
