package com.example.cafeProject.member;

import com.example.cafeProject.validation.ManagementOnly;
import com.example.cafeProject.validation.ValidationGroups;
import com.example.exception.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final String basePath = "member"; // 기본으로 사용할 링크

    // 로그 찍는 용도 그 이상도 그 이하도 아님. log.***은 전부 무시해도 됨!!
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    @ManagementOnly
    @GetMapping("/list")
    public String list(
            Model model,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication
    ) {
        log.debug("list 호출됨. 호출자 : {}", safeName(authentication));
        Page<Member> list = memberService.list(pageable);
        model.addAttribute("activeMenu", "list");
        model.addAttribute("list", list);
        model.addAttribute("activeMenu", "member");
        return basePath + "/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/view")
    public String view(
            Model model,
            Authentication authentication
    ) {
        log.debug("view 호출됨. 호출자 : {}", safeName(authentication));
        return loadPathOrRedirect(authentication, model, basePath+"/view");
    }

    @ManagementOnly
    @GetMapping("/view/{id}")
    public String view(
            Model model,
            @PathVariable int id,
            Authentication authentication
    ) {
        log.debug("관리자용 view 호출됨. 호출자 : {}", safeName(authentication));
        try {
            Member entity = memberService.view(id);
            model.addAttribute("data", entity);
            model.addAttribute("sidebarKey", true);
            model.addAttribute("canRoleUpdate", memberService.roleRank(memberService.viewCurrentMember().getRole()) > memberService.roleRank(entity.getRole()));
            return "member/view";
        } catch (AccessDeniedException e) {
            log.warn("현재 로그인 정보를 확인할 수 없음. principal={}", safeName(authentication));
            return "redirect:/";
        } catch (EntityNotFoundException e) {
            log.warn("대상이 되는 Entity를 찾을 수 없음. id={}", id, e);
            return "redirect:/" + basePath + "/list";
        }
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
        log.debug("passwordUpdate 호출됨. 호출자 : {}", safeName(authentication));
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
        log.debug("roleUpdate 호출됨. 호출자 : {}", safeName(authentication));
        Member member = memberService.view(id);

        MemberDTO dto = MemberDTO.builder()
                .id(member.getId())
                .role(member.getRole())
                .build(); // 빌더를 사용해서 id와 role을 세팅

        RoleType[] roles = allowedRoleOptions(authentication);
        if (roles == null) return "redirect:/";

        model.addAttribute("data", dto);
        model.addAttribute("member", member);
        model.addAttribute("roles", roles);

        return basePath + "/roleUpdate";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete")
    public String delete(
            Model model,
            Authentication authentication
    ) {
        log.debug("delete 호출됨. 호출자 : {}", safeName(authentication));
        model.addAttribute("data", new MemberDeleteDTO());  // 입력 폼이 터지지 않게 data란 이름으로 빈 값을 보냄
        return basePath + "/delete";
    }

    /**
     * 탈퇴 사유 페이지
     * - 유저가 탈퇴할 때 사유를 수집해 통계로 만들어 보여주는 페이지
     */
    @ManagementOnly
    @GetMapping("/withdrawalReason")
    public String withdrawalReason(
            @RequestParam(name = "reason", required = false) ReasonType reason,
            @PageableDefault(size = 20, sort = "deletedDate", direction = Sort.Direction.DESC)
            Pageable pageable,
            Model model
    ) {
        List<WithdrawalReasonStat> stats = memberService.withdrawalReasonStats();
        long totalDeleted = 0L;
        for (WithdrawalReasonStat s : stats) totalDeleted += s.getCount();

        Page<Member> deletedPage = memberService.listDeleted(pageable, reason);

        model.addAttribute("activeMenu", "withdrawalReason");
        model.addAttribute("stats", stats);
        model.addAttribute("totalDeleted", totalDeleted);
        model.addAttribute("deletedPage", deletedPage);
        model.addAttribute("reasons", ReasonType.values());
        model.addAttribute("selectedReason", reason);

        return basePath + "/withdrawalReason";
    }

    /**
     * 회원 가입 처리.
     * 접근 조건:
     * - @PreAuthorize("isAnonymous()") : 로그인되지 않은(익명) 사용자만 접근 가능
     * 처리 흐름:
     * 1) OnCreate 그룹 기준 값 검증
     * 2) 검증 실패 시: 공통 메서드(logValidationErrors)로 로그 남기고 회원가입 폼으로 복귀
     * 3) 서비스 레벨에서 회원 저장 시도
     * 4) 예외 유형별 처리:
     *    -ForbiddenUsernameException : 금지된 username → 폼에 필드 에러 표시
     *    - DuplicateValueException : id / email 중복 → 폼에 필드 에러 표시
     *    - IllegalArgumentException : 이외의 기타 유효성 / 인자 오류들
     *    - 그 외 예외 → 런타임 에러 페이지
     */
    @PreAuthorize("isAnonymous()")
    @PostMapping("/createProc")
    public String createProc(
            @Validated(ValidationGroups.OnCreate.class) @ModelAttribute("data") MemberDTO dto,
            BindingResult bindingResult
    ) {
        // 검증 에러가 터지면 logValidationErrors(공통 검증 실패 로그 + 실패시 링크)를 반환
        if (bindingResult.hasErrors()) return logValidationErrors("createProc", "create", bindingResult, "Anonymous");

        try {
            memberService.setInsert(dto); // 서비스에 회원 가입 처리 위임
            return "redirect:/" + basePath + "/login"; // 가입 성공 시 로그인 페이지로 이동
        } catch (ForbiddenUsernameException e) { // 금지된 username으로 가입 시도
            bindingResult.rejectValue("username", "forbidden", e.getMessage());
            return basePath + "/create";
        } catch (DuplicateValueException e) { // 이미 사용중인 id / email 오류
            if (e.getField() != null) bindingResult.rejectValue(e.getField(), "duplicate", e.getMessage());
            else bindingResult.reject("duplicate", e.getMessage());
            return basePath + "/create";
        } catch (IllegalArgumentException e) { // 남아있는 기타 유효성/인자 오류들
            log.warn(e.getMessage());
            bindingResult.reject("joinError", e.getMessage());
            return basePath + "/create";
        } catch (Exception e) { // 그 외 치명적인 오류
            log.error("[createProc] 예상하지 못 한 오류 : {}", e.getMessage(), e);
            return "redirect:/error/runtimeErrorPage";
        }
    }

    /**
     * 비밀번호 변경 처리.
     * 접근 조건:
     * - @PreAuthorize("isAuthenticated()") : 로그인된 사용자만 접근 가능
     * 처리 흐름:
     * 1) 값 검증
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
        // 검증 에러가 터지면 logValidationErrors(공통 검증 실패 로그 + 실패시 링크)를 반환
        if (bindingResult.hasErrors()) return logValidationErrors("passwordProc", "passwordUpdate", bindingResult, safeName(authentication));
        // Valid로 처리하기 애매한 "새 비밀번호 / 확인 비밀번호" 일치 여부 수동 검증
        if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
            log.warn("[비밀번호 변경 검증 실패] 새 비밀번호 불일치");
            bindingResult.rejectValue("confirmPassword", "mismatch", "비밀번호 확인이 일치하지 않습니다.");
            return basePath + "/passwordUpdate";
        }

        try {
            // @PreAuthorize 때문에 null이 아니어야 하지만 방어 코드 차원에서 호출
            if (memberService.isNotLogin(authentication)) return "redirect:/member/login";
            // 밴 체크 : 현재 활동중인 유저의 모든 권한 목록을 가져와서 그중 BANNED가 있으면 비밀번호 변경 차단
            if (authentication.getAuthorities().toString().contains("ROLE_BANNED")) {
                log.warn("[비밀번호 변경 차단] BANNED 계정, principal={}", safeName(authentication));
                return "redirect:/";
            }
            memberService.setUpdate(authentication, passwordChangeDTO); // 서비스에 실제 비밀번호 변경 로직 위임
            return "redirect:/"; // 성공 시 메인으로 이동
        } catch (AccessDeniedException e) { // 현재 로그인 정보를 확인할 수 없음
            log.warn("[passwordProc] 현재 로그인 정보를 확인할 수 없음. principal={}", safeName(authentication), e);
            return "redirect:/";
        } catch (EntityNotFoundException e) { // 이미 삭제되었거나 존재하지 않는 계정 오류
            log.warn("[passwordProc] 대상이 되는 Entity를 찾을 수 없음. principal={}", safeName(authentication), e);
            return "redirect:/";
        } catch (WrongPasswordException e) { // 현재 비밀번호가 일치하지 않는 오류
            log.warn("[passwordProc] 현재 비밀번호 불일치. username={}", safeName(authentication));
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
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        // 검증 에러가 터지면 logValidationErrors(공통 검증 실패 로그 + 실패시 링크)를 반환
        if (bindingResult.hasErrors()) return logValidationErrors("roleProc", "roleUpdate", bindingResult, safeName(authentication));

        try {
            Member admin = memberService.viewCurrentMember(authentication);
            memberService.updateRoleType(dto, admin);
            redirectAttributes.addFlashAttribute("msg", "권한 변경됨");
            return "redirect:/" + basePath + "/view/" + dto.getId();
        } catch (AccessDeniedException e) { // 현재 로그인 정보를 확인할 수 없음
            log.warn("[roleProc] 현재 로그인 정보를 확인할 수 없음. principal={}", safeName(authentication), e);
            return "redirect:/";
        } catch (EntityNotFoundException e) { // 이미 삭제되었거나 존재하지 않는 계정 오류
            log.warn("[roleProc] 대상이 되는 Entity를 찾을 수 없음. principal={}", safeName(authentication), e);
            return "redirect:/";
        } catch (IllegalArgumentException e) { // 잘못된 변경값
            log.warn("[roleProc] 잘못된 변경값. {}", e.getMessage());
            // 다시 그 회원의 정보와 역할 목록을 모델에 담아서 폼 재표시
            RoleType[] roles = allowedRoleOptions(authentication);
            if (roles == null) return "redirect:/";

            model.addAttribute("member", memberService.view(dto.getId()));
            model.addAttribute("roles", roles);
            // 글로벌 에러로 추가 (필요시 코드 분리 가능)
            bindingResult.reject("roleChangeError", e.getMessage());
            return basePath + "/roleUpdate";
        } catch (PermissionDeniedException e) { // 자신의 권한 이상의 작업을 수행
            log.warn("[roleProc] 변경 권한 부족. {}", e.getMessage());
            // 다시 그 회원의 정보와 역할 목록을 모델에 담아서 폼 재표시
            RoleType[] roles = allowedRoleOptions(authentication);
            if (roles == null) return "redirect:/";

            model.addAttribute("member", memberService.view(dto.getId()));
            model.addAttribute("roles", roles);
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
     * 1) 값 검증
     * 2) 검증 실패 시: 공통 메서드(logValidationErrors)로 로그 남기고 탈퇴 확인 폼으로 복귀
     * 3) BANNED 계정 여부 검사
     * 4) 서비스에 실제 탈퇴 처리 위임
     * 5) 예외 유형별 처리:
     *    - AccessDeniedException : 현재 로그인한 유저 정보 없음 → 메인으로 이동
     *    - EntityNotFoundException : 회원 정보 없음 → 메인으로 이동
     *    - WrongPasswordException : 현재 비밀번호 불일치 → 폼에 필드 에러 표시
     *    - 그 외 예외 → 런타임 에러 페이지
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/deleteProc")
    public String deleteProc(
            @Valid @ModelAttribute("data") MemberDeleteDTO dto,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request
    ) {
        // 검증 에러가 터지면 logValidationErrors(공통 검증 실패 로그 + 실패시 링크)를 반환
        if (bindingResult.hasErrors()) return logValidationErrors("deleteProc", "delete", bindingResult, safeName(authentication));

        try {
            // @PreAuthorize 때문에 null이 아니어야 하지만 방어 코드 차원에서 호출
            if (memberService.isNotLogin(authentication)) return "redirect:/member/login";
            // 밴 체크 : 현재 활동중인 유저의 모든 권한 목록을 가져와서 그중 BANNED가 있으면 회원 탈퇴 차단
            if (authentication.getAuthorities().toString().contains("ROLE_BANNED")) {
                log.warn("[회원 탈퇴 차단] BANNED 계정, principal={}", safeName(authentication));
                return "redirect:/";
            }
            memberService.setDelete(authentication, dto);

            // 소프트 삭제 후 현재 세션/인증을 정리
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();

            return "redirect:/";
        } catch (AccessDeniedException e) { // 현재 로그인 정보를 확인할 수 없음
            log.warn("[deleteProc] 현재 로그인 정보를 확인할 수 없음. principal={}", safeName(authentication), e);
            return "redirect:/";
        } catch (EntityNotFoundException e) { // 이미 삭제되었거나 존재하지 않는 계정 오류
            log.warn("[deleteProc] 대상이 되는 Entity를 찾을 수 없음. principal={}", safeName(authentication), e);
            return "redirect:/";
        } catch (WrongPasswordException e) {
            log.warn("[deleteProc] 현재 비밀번호 불일치. username={}", safeName(authentication));
            bindingResult.rejectValue("password", "wrong", e.getMessage());
            return basePath + "/delete";
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

    /**
     * 모든 POST 검증 실패 시 공통으로 사용하는 로깅 + 뷰 반환 메서드.
     * action: 로그에 찍힐 작업 이름, path: 실패 시 다시 보여줄 뷰 경로.
     */
    private String logValidationErrors(String action, String path, BindingResult bindingResult, String principal) {
        log.warn("[{} 검증 실패] principal={}", action, principal);
        bindingResult.getFieldErrors().forEach(error ->
                log.warn("[{} 검증 실패 상세] - field: {}, message: {}",
                        action,
                        error.getField(),
                        error.getDefaultMessage())
        ); // 모든 검증 오류를 log.warn으로 출력
        return basePath + "/" + path;
    }

    /**
     * 현재 로그인한 사용자의 Member를 조회해서 model에 "data"로 담고 지정된 뷰로 이동.
     * 대상이 없으면 경고 로그를 남기고 메인으로 redirect.
     */
    private String loadPathOrRedirect(Authentication authentication, Model model, String path) {
        try {
            Member entity = memberService.viewCurrentMember(authentication);
            model.addAttribute("data", entity);
            return path;
        } catch (AccessDeniedException e) { // 현재 로그인 정보를 확인할 수 없음
            log.warn("[loadPathOrRedirect] 현재 로그인 정보를 확인할 수 없음. principal={}", safeName(authentication));
            return "redirect:/";
        } catch (EntityNotFoundException e) { // 이미 삭제되었거나 존재하지 않는 계정 오류
            log.warn("[loadPathOrRedirect] 대상이 되는 Entity를 찾을 수 없음. principal={}", safeName(authentication), e);
            return "redirect:/";
        }
    }

    /**
    * 현재 로그인한 사용자의 권한(Authorities)에 따라
    * 권한 변경 화면에서 선택 가능한 RoleType 목록을 반환한다.
    */
    private RoleType[] allowedRoleOptions(Authentication authentication) {
        // ADMIN이면: MANAGER까지 부여 가능
        if (authentication.getAuthorities().toString().contains("ROLE_ADMIN"))
            return new RoleType[]{RoleType.BANNED, RoleType.USER, RoleType.MANAGER};

        // MANAGER이면: USER/BANNED만 변경 가능
        if (authentication.getAuthorities().toString().contains("ROLE_MANAGER"))
            return new RoleType[]{RoleType.BANNED, RoleType.USER};

        // 그 외 권한(일반 USER 등)은 권한 변경 기능 사용 불가
        return null;
    }

    /**
     * authentication에서 getName 메서드를 실행할 때 NPE가 날 수 있기에 사용하는 메서드.
     * authentication이 null일 경우 getName을 실행하는 대신 "null"이라는 문자열을 반환한다.
     */
    private String safeName(Authentication authentication) {
        return (authentication == null) ? "null" : authentication.getName();
    }
}
