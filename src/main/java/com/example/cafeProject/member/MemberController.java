package com.example.cafeProject.member;

import com.example.cafeProject.validation.ManagementOnly;
import com.example.cafeProject.validation.ValidationGroups;
import com.example.exception.EntityNotFoundException;
import com.example.exception.PermissionDeniedException;
import com.example.exception.WrongPasswordException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
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
    private final MemberService memberService;
    private final String basePath = "member"; // 기본으로 사용할 링크

    // 로그 찍는 용도 그 이상도 그 이하도 아님. log.***은 전부 무시해도 됨!!
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    // 모든 POST 검증 실패 시 공통으로 사용하는 로깅 + 뷰 반환 메서드.
    // action: 로그에 찍힐 작업 이름, path: 실패 시 다시 보여줄 뷰 경로.
    private String logValidationErrors(String action, String path, BindingResult bindingResult) {
        log.warn("[{} 검증 실패]", action);
        bindingResult.getFieldErrors().forEach(error ->
                log.warn("[{} 검증 실패 상세] - field: {}, value: {}, message: {}",
                        action,
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage())
        ); // 모든 검증 오류를 log.warn으로 출력
        return basePath + "/" + path;
    }

    // id로 Member를 조회해서 존재하면 model에 "data"로 담고 지정된 뷰로 이동.
    // 대상이 없으면 경고 로그를 남기고 메인으로 redirect.
    private String loadPathOrRedirect(int id, Model model, String path) {
        try {
            Member entity = memberService.view(id);
            model.addAttribute("data", entity);
            return path;
        } catch (EntityNotFoundException e) {
            log.warn("대상이 되는 Entity를 찾을 수 없음. id={}", id, e);
            return "redirect:/";
        }
    }
    // 현재 로그인한 사용자의 Member를 조회해서 model에 "data"로 담고 지정된 뷰로 이동.
    // 대상이 없으면 경고 로그를 남기고 메인으로 redirect.
    private String loadPathOrRedirect(Authentication authentication, Model model, String path) {
        try {
            Member entity = memberService.viewCurrentMember(authentication);
            model.addAttribute("data", entity);
            return path;
        } catch (AccessDeniedException e) { // 현재 로그인 정보를 확인할 수 없음
            log.warn("[loadPathOrRedirect] 현재 로그인 정보를 확인할 수 없음. principal={}", authentication != null ? authentication.getName() : "null");
            return "redirect:/";
        } catch (EntityNotFoundException e) { // 이미 삭제되었거나 존재하지 않는 계정 오류
            log.warn("대상이 되는 Entity를 찾을 수 없음. principal={}", authentication != null ? authentication.getName() : "null", e);
            return "redirect:/";
        }
    }

    @ManagementOnly
    @GetMapping("/list")
    public String list(
            Model model,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication
    ) {
        log.debug("list 호출됨. 호출자 : {}", authentication.getName());
        Page<Member> list = memberService.list(pageable);
        model.addAttribute("list", list);
        return basePath + "/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/view")
    public String view(
            Model model,
            Authentication authentication
    ) {
        log.debug("view 호출됨. 호출자 : {}", authentication.getName());
        return loadPathOrRedirect(authentication, model, basePath+"/view");
    }

    @ManagementOnly
    @GetMapping("/view/{id}")
    public String view(
            Model model,
            @PathVariable int id,
            Authentication authentication
    ) {
        log.debug("관리자용 view 호출됨. 호출자 : {}", authentication.getName());
        return loadPathOrRedirect(id, model, basePath+"/view");
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/create")
    public String create(Model model) {
        log.debug("create 호출됨.");
        model.addAttribute("data", new MemberDTO()); // 입력 폼이 터지지 않게 data란 이름으로 빈 값을 보냄
        return basePath + "/create";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/passwordUpdate")
    public String passwordUpdate(
            Model model,
            Authentication authentication
    ) {
        log.debug("passwordUpdate 호출됨. 호출자 : {}", authentication.getName());
        model.addAttribute("data", new PasswordChangeDTO()); // 입력 폼이 터지지 않게 data란 이름으로 빈 값을 보냄
        return basePath + "/passwordUpdate";
    }

    @ManagementOnly
    @GetMapping("/roleUpdate/{id}")
    public String roleUpdate(
            @PathVariable Integer id,
            Authentication authentication,
            Model model
    ) {
        log.debug("roleUpdate 호출됨. 호출자 : {}", authentication.getName());
        Member member = memberService.view(id);

        MemberDTO dto = MemberDTO.builder()
                .id(member.getId())
                .role(member.getRole())
                .build(); // 빌더를 사용해서 id와 role을 세팅

        model.addAttribute("data", dto);
        model.addAttribute("member", member);
        if (authentication.getAuthorities().toString().contains("ROLE_ADMIN")) {
            model.addAttribute("roles", new RoleType[]{RoleType.BANNED, RoleType.USER, RoleType.MANAGER});
        } else if (authentication.getAuthorities().toString().contains("ROLE_MANAGER")) {
            model.addAttribute("roles", new RoleType[]{RoleType.BANNED, RoleType.USER});
        } else {
            return "redirect:/";
        }

        return basePath + "/roleUpdate";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete")
    public String delete(
            Model model,
            Authentication authentication
    ) {
        log.debug("delete 호출됨. 호출자 : {}", authentication.getName());
        model.addAttribute("data", new MemberDeleteDTO());  // 입력 폼이 터지지 않게 data란 이름으로 빈 값을 보냄
        return basePath + "/delete";
    }

    /**
     * 회원 가입 처리.
     * 접근 조건:
     * - @PreAuthorize("isAnonymous()") : 로그인되지 않은(익명) 사용자만 접근 가능
     * 처리 흐름:
     * 1) OnCreate 그룹 기준 값 검증 + DTO 정규화 수행
     * 2) 검증 실패 시: 공통 메서드(logValidationErrors)로 로그 남기고 회원가입 폼으로 복귀
     * 3) 서비스 레벨에서 회원 저장 시도
     * 4) 예외 유형별 처리:
     *    - IllegalArgumentException : id / email 중복 → 폼에 필드 에러 표시
     *    - 그 외 예외 → 런타임 에러 페이지
     */
    @PreAuthorize("isAnonymous()")
    @PostMapping("/createProc")
    public String createProc(
            @Validated(ValidationGroups.OnCreate.class) @ModelAttribute("data") MemberDTO dto,
            BindingResult bindingResult
    ) {
        dto.normalize();
        // 검증 에러가 터지면 logValidationErrors(공통 검증 실패 로그 + 실패시 링크)를 반환
        if (bindingResult.hasErrors()) return logValidationErrors("createProc", "create", bindingResult);

        try {
            memberService.setInsert(dto); // 서비스에 회원 가입 처리 위임
            return "redirect:/" + basePath + "/login"; // 가입 성공 시 로그인 페이지로 이동
        } catch (IllegalArgumentException e) { // 이미 사용중인 id / email 오류
            log.warn(e.getMessage());

            // 메시지 내용에 따라 username / email 중 어떤 필드 에러로 보여줄지 결정
            if (e.getMessage().contains("id")) {
                bindingResult.rejectValue("username", "duplicate", e.getMessage());
            } else if (e.getMessage().contains("email")) {
                bindingResult.rejectValue("email", "duplicate", e.getMessage());
            } else {
                // 위 두 가지 유형이 아닌 경우, 글로벌 에러로 처리
                bindingResult.reject("joinError", e.getMessage());
            }

            return basePath + "/create";
        } catch (Exception e) { // 그 외 치명적인 오류
            log.error("[createProc] 예상하지 못 한 오류 : {}", e.getMessage());
            return "redirect:/error/runtimeErrorPage";
        }
    }

    /**
     * 비밀번호 변경 처리.
     * 접근 조건:
     * - @PreAuthorize("isAuthenticated()") : 로그인된 사용자만 접근 가능
     * 처리 흐름:
     * 1) 값 검증 + DTO 정규화 수행
     * 2) 검증 실패 시: 공통 메서드(logValidationErrors)로 로그 남기고 비밀번호 변경 폼으로 복귀
     * 3) 새 비밀번호 / 비밀번호 확인 일치 여부 수동 검사
     * 4) BANNED 계정 여부 확인
     * 5) 서비스에 비밀번호 변경 요청
     * 6) 예외 유형별 처리:
     *    - AccessDeniedException : 현재 로그인한 유저 정보 없음 → 메인으로 이동
     *    - EntityNotFoundException : 대상 회원 없음 → 메인으로 리다이렉트
     *    - WrongPasswordException  : 현재 비밀번호 불일치 → 폼에 필드 에러 표시
     *    - 그 외 예외 → 런타임 에러 페이지로 이동
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/passwordProc")
    public String passwordProc(
            @Valid @ModelAttribute("data") PasswordChangeDTO passwordChangeDTO,
            BindingResult bindingResult,
            Authentication authentication
    ) {
        passwordChangeDTO.normalize();
        // 검증 에러가 터지면 logValidationErrors(공통 검증 실패 로그 + 실패시 링크)를 반환
        if (bindingResult.hasErrors()) return logValidationErrors("passwordProc", "passwordUpdate", bindingResult);
        // Valid로 처리하기 애매한 "새 비밀번호 / 확인 비밀번호" 일치 여부 수동 검증
        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
            log.warn("[비밀번호 변경 검증 실패] 새 비밀번호 불일치");
            bindingResult.rejectValue("confirmPassword", "mismatch", "비밀번호 확인이 일치하지 않습니다.");
            return basePath + "/passwordUpdate";
        }

        try {
            // @PreAuthorize 때문에 null이 아니어야 하지만 방어 코드 차원에서 호출
            if (authentication == null || !authentication.isAuthenticated()) throw new AccessDeniedException("현재 로그인 정보를 확인할 수 없습니다.");
            // 밴 체크 : 현재 활동중인 유저의 모든 권한 목록을 가져와서 그중 BANNED가 있으면 비밀번호 변경 차단
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_BANNED"))) {
                log.warn("[비밀번호 변경 차단] BANNED 계정, principal={}", authentication.getName());
                return "redirect:/";
            }
            memberService.setUpdate(authentication, passwordChangeDTO); // 서비스에 실제 비밀번호 변경 로직 위임
            return "redirect:/"; // 성공 시 메인으로 이동
        } catch (AccessDeniedException e) { // 현재 로그인 정보를 확인할 수 없음
            log.warn("[passwordProc] 현재 로그인 정보를 확인할 수 없음. principal={}", authentication != null ? authentication.getName() : "null", e);
            return "redirect:/";
        } catch (EntityNotFoundException e) { // 이미 삭제되었거나 존재하지 않는 계정 오류
            log.warn("[passwordProc] 대상이 되는 Entity를 찾을 수 없음. principal={}", authentication != null ? authentication.getName() : "null", e);
            return "redirect:/";
        } catch (WrongPasswordException e) { // 현재 비밀번호가 일치하지 않는 오류
            log.warn("[passwordProc] 현재 비밀번호 불일치. username={}", authentication != null ? authentication.getName() : "null");
            bindingResult.rejectValue("currentPassword", "wrong", e.getMessage());
            return basePath + "/passwordUpdate";
        } catch (Exception e) { // 그 외 치명적인 오류
            log.error("[passwordProc] 예상하지 못한 오류 : {}", e.getMessage());
            return "redirect:/error/runtimeErrorPage";
        }
    }

    /**
     * 관리자용 회원 권한 변경 처리.
     * 접근 조건:
     * - @ManagementOnly : ADMIN / MANAGER 등 관리 권한을 가진 사용자만 접근 가능
     * 처리 흐름:
     * 1) OnUpdate 그룹 기준 값 검증 수행
     * 2) 검증 실패 시: 공통 메서드(logValidationErrors)로 로그 남기고 권한 변경 폼으로 복귀
     * 3) 현재 로그인한 관리자의 Member 엔티티 조회
     * 4) 서비스에 권한 변경 로직 위임
     * 5) 예외 유형별 처리:
     *    - AccessDeniedException : 현재 로그인한 유저 정보 없음 → 메인으로 이동
     *    - EntityNotFoundException : 대상 회원 또는 관리자 정보 없음 → 메인으로 이동
     *    - IllegalArgumentException : 잘못된 권한 변경 요청 → 폼 재표시 + 에러 메시지
     *    - PermissionDeniedException : 자신의 권한보다 높은 작업 시도 → 폼 재표시 + 에러 메시지
     *    - 그 외 예외 → 런타임 에러 페이지
     */
    @ManagementOnly
    @PostMapping("/roleProc")
    public String roleProc(
            @Validated(ValidationGroups.OnUpdate.class) @ModelAttribute("data") MemberDTO dto,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        // 검증 에러가 터지면 logValidationErrors(공통 검증 실패 로그 + 실패시 링크)를 반환
        if (bindingResult.hasErrors()) return logValidationErrors("roleProc", "roleUpdate", bindingResult);

        try {
            Member admin = memberService.viewCurrentMember(authentication);
            memberService.updateRoleType(dto, admin);
            return "redirect:/" + basePath + "/view/" + dto.getId();
        } catch (AccessDeniedException e) { // 현재 로그인 정보를 확인할 수 없음
            log.warn("[roleProc] 현재 로그인 정보를 확인할 수 없음. principal={}", authentication != null ? authentication.getName() : "null", e);
            return "redirect:/";
        } catch (EntityNotFoundException e) { // 이미 삭제되었거나 존재하지 않는 계정 오류
            log.warn("[roleProc] 대상이 되는 Entity를 찾을 수 없음. principal={}", authentication != null ? authentication.getName() : "null", e);
            return "redirect:/";
        } catch (IllegalArgumentException e) { // 잘못된 변경값
            log.warn("[roleProc] 잘못된 변경값. {}", e.getMessage());
            // 다시 그 회원의 정보와 역할 목록을 모델에 담아서 폼 재표시
            model.addAttribute("member", memberService.view(dto.getId()));
            model.addAttribute("roles", RoleType.values());
            // 글로벌 에러로 추가 (필요시 코드 분리 가능)
            bindingResult.reject("roleChangeError", e.getMessage());
            return basePath + "/roleUpdate";
        } catch (PermissionDeniedException e) { // 자신의 권한 이상의 작업을 수행
            log.warn("[roleProc] 변경 권한 부족. {}", e.getMessage());
            // 다시 그 회원의 정보와 역할 목록을 모델에 담아서 폼 재표시
            model.addAttribute("member", memberService.view(dto.getId()));
            model.addAttribute("roles", RoleType.values());
            // 글로벌 에러로 추가 (필요시 코드 분리 가능)
            bindingResult.reject("roleChangeError", e.getMessage());
            return basePath + "/roleUpdate";
        } catch (Exception e) { // 그 외 치명적인 오류
            log.error("[roleProc] 예상하지 못한 오류 : {}", e.getMessage());
            return "redirect:/error/runtimeErrorPage";
        }
    }

    /**
     * 회원 탈퇴 처리.
     * 접근 조건:
     * - @PreAuthorize("isAuthenticated()") : 로그인된 사용자만 접근 가능
     * 처리 흐름:
     * 1) DTO 정규화 + Bean Validation 수행
     * 2) 검증 실패 시: 공통 메서드(logValidationErrors)로 로그 남기고 탈퇴 확인 폼으로 복귀
     * 3) BANNED 계정 여부 검사
     * 4) 서비스에 실제 탈퇴 처리 위임
     * 5) 예외 유형별 처리:
     *    - AccessDeniedException : 현재 로그인한 유저 정보 없음 → 메인으로 이동
     *    - EntityNotFoundException : 회원 정보 없음 → 메인으로 이동
     *    - 그 외 예외 → 런타임 에러 페이지
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/deleteProc")
    public String deleteProc(
            @Valid @ModelAttribute("data") MemberDeleteDTO dto,
            BindingResult bindingResult,
            Authentication authentication
    ) {
        dto.normalize();
        // 검증 에러가 터지면 logValidationErrors(공통 검증 실패 로그 + 실패시 링크)를 반환
        if (bindingResult.hasErrors()) return logValidationErrors("deleteProc", "delete", bindingResult);

        try {
            // @PreAuthorize 때문에 null이 아니어야 하지만 방어 코드 차원에서 호출
            if (authentication == null || !authentication.isAuthenticated()) throw new AccessDeniedException("현재 로그인 정보를 확인할 수 없습니다.");
            // 밴 체크 : 현재 활동중인 유저의 모든 권한 목록을 가져와서 그중 BANNED가 있으면 회원 탈퇴 차단
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_BANNED"))) {
                log.warn("[회원 탈퇴 차단] BANNED 계정, principal={}", authentication.getName());
                return "redirect:/";
            }
            memberService.setDelete(authentication, dto);
            return "redirect:/";
        } catch (AccessDeniedException e) { // 현재 로그인 정보를 확인할 수 없음
            log.warn("[deleteProc] 현재 로그인 정보를 확인할 수 없음. principal={}", authentication != null ? authentication.getName() : "null", e);
            return "redirect:/";
        } catch (EntityNotFoundException e) { // 이미 삭제되었거나 존재하지 않는 계정 오류
            log.warn("[deleteProc] 대상이 되는 Entity를 찾을 수 없음. principal={}", authentication != null ? authentication.getName() : "null", e);
            return "redirect:/";
        } catch (Exception e) { // 그 외 치명적인 오류
            log.error("[deleteProc] 예상하지 못한 오류 : {}", e.getMessage());
            return "redirect:/error/runtimeErrorPage";
        }
    }

    // 로그인 관련
    @GetMapping("/login")
    public String login(
            @ModelAttribute("data") MemberDTO dto
    ) {
        return basePath + "/login";
    }

    @GetMapping("/login/error")
    public String loginError(
            @ModelAttribute("data") MemberDTO dto,
            Model model
    ) {
        model.addAttribute("loginError", true); // 로그인시 아이디/비밀번호가 잘못되면 안내하기 위함
        return basePath + "/login";
    }
}
