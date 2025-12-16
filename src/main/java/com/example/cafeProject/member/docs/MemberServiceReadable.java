package com.example.cafeProject.member.docs;

import com.example.cafeProject.member.*;
import com.example.exception.EntityNotFoundException;
import com.example.exception.ForbiddenUsernameException;
import com.example.exception.PermissionDeniedException;
import com.example.exception.WrongPasswordException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Optional;

/**
 * 읽기 전용(문서용) MemberService
 * - 실제 서비스 빈이 아님 (의도적으로 @Service 없음)
 * - 로그 제거 버전: "로직만" 보기 위한 참고용 파일
 * 실제 동작은 MemberService가 담당
 * 해당 파일은 2025년 12월 16일에 마지막으로 수정됨
 */
// @Service
// @RequiredArgsConstructor
@SuppressWarnings("unused")
@Deprecated(since = "2025-12-16", forRemoval = false)
public final class MemberServiceReadable {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private MemberServiceReadable(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        throw new UnsupportedOperationException("Docs-only class. Use MemberService instead.");
    }

    /** 회원가입시 아이디에 포함할 수 없는 문자열 목록 */
    private static final String[] FORBIDDEN_CONTAINS = {
            "admin", "anonymous", "banned", "config",
            "csrf", "empty", "exception", "manager",
            "owner", "root", "system", "undefined"
    };

    /** 회원가입시 불가능한 아이디 목록 */
    private static final String[] FORBIDDEN_EQUALS = {
            "con", "prn", "aux", "nul", "null",
            "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
            "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"
    };


    /** 전체 회원 목록 조회 (페이징 포함) */
    public Page<Member> list(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }

    /** 회원 상세 (id 기준) - 없으면 EntityNotFoundException */
    public Member view(int id) throws EntityNotFoundException {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 id=" + id));
    }

    /** Optional 버전 상세 조회 */
    public Optional<Member> viewOptional(int id) {
        return memberRepository.findById(id);
    }

    /**
     * 현재 로그인한 사용자의 Member 엔티티 조회.
     * - 인증 정보가 없거나 익명이면 AccessDeniedException
     * - username으로 조회 결과가 없으면 EntityNotFoundException
     */
    public Member viewCurrentMember(Authentication authentication) {
        if (isNotLogin(authentication)) {
            throw new AccessDeniedException("현재 로그인 정보를 확인할 수 없습니다.");
        }
        String username = authentication.getName();
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 username=" + username));
    }

    /** 회원 등록 */
    @Transactional
    public Member setInsert(MemberDTO dto) {
        beforeCreate(dto);
        Member entity = toEntity(dto);
        afterCreate(dto, entity);
        return memberRepository.save(entity);
    }

    /** 비밀번호 변경(회원 정보 수정) */
    @Transactional
    public Member setUpdate(Authentication authentication, PasswordChangeDTO dto) {
        Member entity = viewCurrentMember(authentication);
        beforeUpdate(dto, entity);
        updateEntity(entity, dto);
        afterUpdate(dto, entity);
        return memberRepository.save(entity);
    }

    /** 회원 탈퇴(삭제) */
    @Transactional
    public void setDelete(Authentication authentication, MemberDeleteDTO dto) {
        Member entity = viewCurrentMember(authentication);
        beforeDelete(entity, dto);
        memberRepository.delete(entity);
        afterDelete(entity, dto);
    }

    /** DTO → Entity 변환 */
    private Member toEntity(MemberDTO dto) {
        return Member.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .role(dto.getRole())
                .build();
    }

    /** 비밀번호 변경 DTO 반영 */
    private void updateEntity(Member e, PasswordChangeDTO d) {
        if (d.getNewPassword() != null && !d.getNewPassword().isBlank()) {
            e.setPassword(passwordEncoder.encode(d.getNewPassword()));
        }
    }

    private void beforeCreate(MemberDTO dto) {
        dto.normalize();

        checkForbiddenUsername(dto.getUsername());

        if (memberRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 id");
        }
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 email");
        }

        // role 기본값: USER
        dto.setRole(RoleType.USER);
    }

    private void afterCreate(MemberDTO dto, Member entity) {
        // 후처리(문서용: 생략)
    }

    private void beforeUpdate(PasswordChangeDTO dto, Member entity) {
        dto.normalize();
        checkPassword(dto.getCurrentPassword(), entity);
    }

    private void afterUpdate(PasswordChangeDTO dto, Member entity) {
        // 후처리(문서용: 생략)
    }

    private void beforeDelete(Member entity, MemberDeleteDTO dto) {
        dto.normalize();
        checkPassword(dto.getPassword(), entity);
    }

    private void afterDelete(Member entity, MemberDeleteDTO dto) {
        // 후처리(문서용: 생략)
    }

    /** 금지/예약어 username 차단 */
    private void checkForbiddenUsername(String username) throws ForbiddenUsernameException {
        if (username == null) return;

        String compact = username.trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");

        for (String token : FORBIDDEN_EQUALS) {
            if (compact.equals(token)) {
                throw new ForbiddenUsernameException("혼동될 수 있는 id.");
            }
        }
        for (String token : FORBIDDEN_CONTAINS) {
            if (compact.contains(token)) {
                throw new ForbiddenUsernameException("혼동될 수 있는 id.");
            }
        }
    }

    /** 로그인 상태가 아니면 true */
    public boolean isNotLogin(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
    }

    /** 비밀번호 일치 확인 */
    private void checkPassword(String password, Member entity) throws WrongPasswordException {
        if (password != null && !passwordEncoder.matches(password, entity.getPassword())) {
            throw new WrongPasswordException("올바르지 않은 현재 비밀번호");
        }
    }

    /** 권한 변경 */
    @Transactional
    public void updateRoleType(MemberDTO d, Member admin) {
        Member entity = view(d.getId());

        RoleType oldRole = entity.getRole();
        RoleType newRole = d.getRole();
        RoleType adminRole = admin.getRole();

        if (oldRole == newRole) {
            throw new IllegalArgumentException("이미 같은 권한입니다.");
        }
        if (admin.getId().equals(entity.getId())) {
            throw new IllegalArgumentException("자기 자신의 권한은 변경할 수 없습니다.");
        }

        if (adminRole == RoleType.ADMIN) {
            if (oldRole == RoleType.ADMIN) {
                throw new PermissionDeniedException("ADMIN의 권한은 변경할 수 없습니다.");
            }
            if (newRole == RoleType.ADMIN) {
                throw new PermissionDeniedException("ADMIN으로 권한을 변경할 수 없습니다.");
            }
        } else if (adminRole == RoleType.MANAGER) {
            if (oldRole == RoleType.ADMIN || oldRole == RoleType.MANAGER) {
                throw new PermissionDeniedException("MANAGER는 ADMIN/MANAGER 계정의 권한을 변경할 수 없습니다.");
            }
            if (newRole == RoleType.ADMIN || newRole == RoleType.MANAGER) {
                throw new PermissionDeniedException("MANAGER는 ADMIN/MANAGER 권한을 부여할 수 없습니다.");
            }
        } else {
            throw new PermissionDeniedException("권한 변경 권한이 없습니다.");
        }

        entity.setRole(newRole);
        memberRepository.save(entity);
    }
}
