package com.example.cafeProject.member;

import com.example.cafeProject._boardTest.Board;
import com.example.exception.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 로그에 클래스명을 찍기 위해 미리 상수로 빼둔 클래스 객체
    private static final Class<MemberService> memberServiceClass = MemberService.class;
    // 로그 찍는 용도 그 이상도 그 이하도 아님. log.***은 전부 무시해도 됨!!
    private static final Logger log = LoggerFactory.getLogger(memberServiceClass);

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

    /**
     * 전체 회원 목록 조회 (페이징 포함).
     * - page, size, sort 조건을 로그로 남기고
     * - 결과 Page 정보(전체 개수, 전체 페이지, 현재 페이지 요소 수)도 함께 로그로 남김
     */
    public Page<Member> list(Pageable pageable) {
        log.debug("[{}] 목록 조회 요청, page={}, size={}, sort={}",
                memberServiceClass.getSimpleName(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());
        Page<Member> page = memberRepository.findAll(pageable);
        log.debug("[{}] 목록 조회 결과, totalElements={}, totalPages={}, currentElements={}",
                memberServiceClass.getSimpleName(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumberOfElements());
        return page;
    }

    /**
     * 회원 상세 메서드 (id 기준)
     * - 존재하지 않으면 EntityNotFoundException 발생
     */
    public Member view(int id) throws EntityNotFoundException {
        log.debug("[{}] Entity 조회 시도, id={}", memberServiceClass.getSimpleName(), id);
        Member entity = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 id=" + id));
        log.debug("[{}] Entity 조회 성공, id={}", memberServiceClass.getSimpleName(), id);
        return entity;
    }

    /**
     * Optional 버전 view 메서드 (id 기준)
     * - 예외를 던지지 않고 Optional로 감싸서 반환.
     * - 호출 측에서 isPresent()/orElse() 등으로 유연하게 처리하고 싶을 때 사용.
     */
    public Optional<Member> viewOptional(int id) {
        log.debug("[{}] Optional 조회 시도, id={}", memberServiceClass.getSimpleName(), id);
        Optional<Member> result = memberRepository.findById(id);
        log.debug("[{}] Optional 조회 결과, id={}, present={}",
                memberServiceClass.getSimpleName(), id, result.isPresent());
        return result;
    }

    /**
     * 현재 로그인한 사용자의 Member 엔티티 조회.
     * - Authentication을 기반으로 username을 가져와 DB에서 조회
     * - 인증 정보가 없거나 익명이면 AccessDeniedException 발생
     * - username으로 조회 결과가 없으면 EntityNotFoundException 발생
     */
    public Member viewCurrentMember(Authentication authentication) {
        // 인증 정보가 없거나 익명 사용자라면 접근 불가
        if (isNotLogin(authentication)) {
            throw new AccessDeniedException("현재 로그인 정보를 확인할 수 없습니다.");
        }
        log.debug("[{}] currentMember 조회 시도, name={}", memberServiceClass.getSimpleName(), authentication.getName());
        Member entity = memberRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 username=" + authentication.getName()));
        log.debug("[{}] currentMember 조회 성공, name={}", memberServiceClass.getSimpleName(), authentication.getName());
        return entity;
    }

    /**
     * 연관관계 연결용 "가짜 엔티티(프록시)"를 가져온다.
     * - findById(id) 처럼 DB를 조회해서 값을 가져오는 게 아니라,
     *   "id = ?" 인 Member가 있다고 가정하고 그 Member를 가리키는 대리 객체(proxy)를 만들어 반환한다.
     * - 따라서 이 메서드 호출 자체로는 보통 SELECT가 나가지 않는다. (쿼리 절약용)
     * - 단, 반환된 객체에서 username/email 같은 "id 외의 필드"를 실제로 읽는 순간
     *   그때 DB를 조회(SELECT)해서 값을 채울 수 있다. (지연 로딩처럼 동작)
     * - 주의:
     *   1) DB에 해당 id가 실제로 없으면, 프록시를 초기화하려는 시점에 예외가 날 수 있다.
     *   2) 트랜잭션 밖에서 프록시의 필드를 읽으면 LazyInitializationException이 날 수 있다.
     */
    public Member getReference(Integer id) {
        return memberRepository.getReferenceById(id);
    }

    /**
     * 회원 등록.
     * 1) beforeCreate(dto) 에서 DTO 정규화 + 중복 체크 + 기본 role 세팅
     * 2) DTO → Entity 변환(toEntity)
     * 3) afterCreate(dto, entity) 에서 로그 등 후처리
     * 4) 저장
     */
    @Transactional
    public Member setInsert(MemberDTO dto) {
        beforeCreate(dto);
        Member entity = toEntity(dto);
        afterCreate(dto, entity);
        return memberRepository.save(entity);
    }

    /**
     * 비밀번호 변경(회원 정보 수정).
     * 1) Authentication 기반으로 현재 회원 조회 (viewCurrentMember)
     * 2) beforeUpdate(dto, entity) 에서 "현재 비밀번호" 검증
     * 3) updateEntity(entity, dto) 에서 실제 필드 갱신 (새 비밀번호 인코딩)
     * 4) afterUpdate(dto, entity) 에서 로그 등 후처리
     * 5) 저장
     */
    @Transactional
    public Member setUpdate(Authentication authentication, PasswordChangeDTO dto) {
        Member entity = viewCurrentMember(authentication);
        beforeUpdate(dto, entity);
        updateEntity(entity, dto);
        afterUpdate(dto, entity);
        return memberRepository.save(entity);
    }

    /**
     * 회원 탈퇴(삭제).
     * 1) Authentication 기반으로 현재 회원 조회
     * 2) beforeDelete(entity, dto) 에서 비밀번호 확인 등 사전 검증
     * 3) delete(entity) 호출
     * 4) afterDelete(entity, dto) 에서 로그 등 후처리
     */
    @Transactional
    public void setDelete(Authentication authentication, MemberDeleteDTO dto) {
        Member entity = viewCurrentMember(authentication);
        beforeDelete(entity, dto);
        memberRepository.delete(entity);
        afterDelete(entity, dto);
    }

    /**
     * DTO → 새로운 Member 엔티티 생성.
     * - 비밀번호는 반드시 passwordEncoder를 사용해 인코딩하여 저장.
     */
    private Member toEntity(MemberDTO dto) {
        return Member.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .role(dto.getRole())
                .grade(dto.getGrade())
                .build();
    }
    /**
     * 기존 Member 엔티티에 비밀번호 변경 DTO를 반영.
     * - 새 비밀번호가 null 또는 공백이면 아무것도 하지 않음.
     * - 값이 있으면 인코딩 후 password 필드 갱신.
     */
    private void updateEntity(Member e, PasswordChangeDTO d) {
        if (d.getNewPassword() != null && !d.getNewPassword().isBlank()) {
            e.setPassword(passwordEncoder.encode(d.getNewPassword()));
        }
    }



    private void beforeCreate(MemberDTO dto) {
        dto.normalize();

        log.info("회원가입 시도: username={}, email={}",
                dto.getUsername(), dto.getEmail());

        // 금지된 username 체크
        checkForbiddenUsername(dto.getUsername());
        // username 중복 체크
        if (memberRepository.existsByUsername(dto.getUsername())) {
            log.warn("회원가입 실패(아이디 중복): username={}", dto.getUsername());
            throw new DuplicateValueException("이미 사용 중인 id", "username", dto.getUsername());
        }
        // email 중복 체크
        if (memberRepository.existsByEmail(dto.getEmail())) {
            log.warn("회원가입 실패(이메일 중복): username={}", dto.getEmail());
            throw new DuplicateValueException("이미 사용 중인 email", "email", dto.getEmail());
        }

        // role 기본값: USER
        dto.setRole(RoleType.USER);
        dto.setGrade(Grade.USER);
    }
    private void afterCreate(MemberDTO dto, Member entity) {
        log.info("회원가입 완료: id={}, username={}, role={}",
                entity.getId(), entity.getUsername(), entity.getRole());
    }
    private void beforeUpdate(PasswordChangeDTO dto, Member entity) {
        dto.normalize();
        log.info("회원 정보 수정 시도: id={}, username={}",
                entity.getId(), entity.getUsername());
        // 수정 전 비밀번호 체크
        checkPassword(dto.getCurrentPassword(), entity);
    }
    private void afterUpdate(PasswordChangeDTO dto, Member entity) {
        log.info("회원 정보 수정 완료: id={}, username={}",
                entity.getId(), entity.getUsername());
    }
    private void beforeDelete(Member entity, MemberDeleteDTO dto) {
        dto.normalize();
        log.warn("회원 삭제 시도: id={}, username={}",
                entity.getId(), entity.getUsername());
        // 삭제 전 비밀번호 체크
        checkPassword(dto.getPassword(), entity);
    }
    private void afterDelete(Member entity, MemberDeleteDTO dto) {
        log.warn("회원 삭제 완료: id={}, username={}",
                entity.getId(), entity.getUsername());
    }

    /**
     * 회원가입 시 "혼동/예약어"로 간주되는 username을 차단하는 메서드.
     * 1) 입력값을 정규화한 compact 문자열을 만든다.
     *    - trim(): 앞/뒤 공백 제거
     *    - toLowerCase(): 소문자 변환
     *    - replace(" ", ""), replace("_", ""), replace("-", ""): 공백/언더바/하이픈 제거
     *      → 예: "hello-anony_mous" 같은 우회 입력을 "helloanonymous"로 합쳐 검사 가능
     * 2) FORBIDDEN_EQUALS: 정확히 일치하면 차단
     * 3) FORBIDDEN_CONTAINS: 포함되면 차단
     * ※ ForbiddenUsernameException : 금지된 username일 경우 발생
     */
    private void checkForbiddenUsername(String username) throws ForbiddenUsernameException {
        // username 자체가 null이면 검증할 대상이 없으므로 종료
        if (username == null) return;

        // 검사 전, 우회 입력을 막기 위해 "비교용 문자열"로 정규화한다.
        // 공백/언더바/하이픈을 제거하여 "anony-mous", "anony_mous" 같은 분리 우회도 탐지
        String compact = username.trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");

        for (String token : FORBIDDEN_EQUALS) {
            if (compact.equals(token)) {
                log.warn("금지된 username(EQUALS) 시도: username={}, compact={}, token={}", username, compact, token);
                throw new ForbiddenUsernameException("혼동될 수 있는 id.");
            }
        }
        for (String token : FORBIDDEN_CONTAINS) {
            if (compact.contains(token)) {
                log.warn("금지된 username(CONTAINS) 시도: username={}, compact={}, token={}", username, compact, token);
                throw new ForbiddenUsernameException("혼동될 수 있는 id.");
            }
        }
    }

    /**
     * 인증 객체가 유효한 로그인 상태인지 판별.
     * 사용처 예:
     * - 로그인 사용자만 접근 가능한 서비스/컨트롤러에서 방어 로직
     * - 현재 사용자 엔티티 조회 전에 선검사
     */
    public boolean isNotLogin(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken;
    }

    /**
     * 현재 사용자의 "실효 권한 목록(Effective Roles)"을 반환.
     * - 게시판 조회/목록 등에서 Board.readRole IN (:roles) 같은 형태로 사용하기 위해,
     *   "내가 볼 수 있는 권한 범위"를 RoleType 배열로 만들어 준다.
     * 주의:
     * - 빈 배열(RoleType[0])은 "아무 권한도 없음"을 의미한다.
     *   따라서 Repository에서 IN (:roles)로 조회할 때 roles가 비면 쿼리가 깨질 수 있으니,
     *   호출 측(Service 등)에서 roles.length == 0이면 Page.empty(...)를 반환하는 방어가 필요하다.
     */
    public RoleType[] getEffectiveRoles(Authentication authentication) {
        if (isNotLogin(authentication)) return new RoleType[]{ RoleType.GUEST };

        if (authentication.getAuthorities().toString().contains("ROLE_BANNED"))
            return new RoleType[0];

        if (authentication.getAuthorities().toString().contains("ROLE_ADMIN"))
            return new RoleType[]{ RoleType.GUEST, RoleType.USER, RoleType.MANAGER, RoleType.ADMIN };

        if (authentication.getAuthorities().toString().contains("ROLE_MANAGER"))
            return new RoleType[]{ RoleType.GUEST, RoleType.USER, RoleType.MANAGER };

        if (authentication.getAuthorities().toString().contains("ROLE_USER"))
            return new RoleType[]{ RoleType.GUEST, RoleType.USER };

        // 예상 못한 케이스라도 최소 공개만
        return new RoleType[]{ RoleType.GUEST };
    }

    /**
     * 사용자가 입력한 비밀번호가 DB에 저장된 비밀번호와 일치하는지 확인.
     * - 일치하지 않으면 WrongPasswordException 발생.
     */
    private void checkPassword(String password, Member entity) throws WrongPasswordException {
        if (password != null && !passwordEncoder.matches(password, entity.getPassword())) {
            log.warn("비밀번호 불일치: memberId={}, username={}", entity.getId(), entity.getUsername());
            throw new WrongPasswordException("올바르지 않은 현재 비밀번호");
        }
    }

    /**
     * 관리자(admin)가 다른 회원의 권한(Role)을 변경하는 메서드.
     * 규칙 요약:
     * - 이미 같은 권한으로 변경하려 하면 예외
     * - 본인 자신의 권한은 변경할 수 없음
     * - ADMIN:
     *   - 다른 ADMIN의 권한을 변경할 수 없음
     *   - 누구를 ADMIN으로 올릴 수도 없음
     * - MANAGER:
     *   - ADMIN / MANAGER 계정의 권한을 변경할 수 없음
     *   - 누구에게도 ADMIN / MANAGER 권한을 부여할 수 없음
     * - 그 외(일반 USER 등)는 권한 변경 작업 자체를 할 수 없음
     * 위 조건 중 하나라도 위반하면 PermissionDeniedException 또는 IllegalArgumentException을 던짐.
     */
    @Transactional
    public void updateRoleType(MemberDTO d, Member admin) {
        // 변경 대상 회원 조회
        Member entity = view(d.getId());

        RoleType oldRole = entity.getRole();    // 기존 권한
        RoleType newRole = d.getRole();         // 변경 요청 권한
        RoleType adminRole = admin.getRole();   // 변경을 수행하는 관리자 권한

        log.info("[updateRoleType] 권한 변경 요청: adminId={}, adminRole={}, targetId={}, oldRole={}, newRole={}",
                admin.getId(), adminRole, entity.getId(), oldRole, newRole);

        if (oldRole == d.getRole()) {
            log.info("[updateRoleType] 이미 같은 권한입니다. id={}, username={}, role={}",
                    entity.getId(), entity.getUsername(), entity.getRole());
            throw new IllegalArgumentException("이미 같은 권한입니다.");
        }
        if (admin.getId().equals(entity.getId())) {
            log.info("[updateRoleType] 자기 자신의 권한은 변경할 수 없습니다. id={}, username={}, role={}",
                    entity.getId(), entity.getUsername(), entity.getRole());
            throw new IllegalArgumentException("자기 자신의 권한은 변경할 수 없습니다.");
        }

        if (adminRole == RoleType.ADMIN) {
            if (oldRole == RoleType.ADMIN) {
                log.info("[updateRoleType] ADMIN의 권한은 변경할 수 없습니다. targetId={}, targetUsername={}",
                        entity.getId(), entity.getUsername());
                throw new PermissionDeniedException("ADMIN의 권한은 변경할 수 없습니다.");
            }
            if (newRole == RoleType.ADMIN) {
                log.info("[updateRoleType] ADMIN으로 권한을 변경할 수 없습니다. targetId={}, targetUsername={}",
                        entity.getId(), entity.getUsername());
                throw new PermissionDeniedException("ADMIN으로 권한을 변경할 수 없습니다.");
            }
        } else if (adminRole == RoleType.MANAGER) {
            if (oldRole == RoleType.ADMIN || oldRole == RoleType.MANAGER) {
                log.info("[updateRoleType] MANAGER는 ADMIN/MANAGER 계정의 권한을 변경할 수 없습니다. adminId={}, adminRole={}, targetId={}, targetRole={}",
                        admin.getId(), adminRole, entity.getId(), oldRole);
                throw new PermissionDeniedException("MANAGER는 ADMIN/MANAGER 계정의 권한을 변경할 수 없습니다.");
            }
            if (newRole == RoleType.ADMIN || newRole == RoleType.MANAGER) {
                log.info("[updateRoleType] MANAGER는 ADMIN/MANAGER 권한을 부여할 수 없습니다. adminId={}, adminRole={}, targetId={}, targetRole={}",
                        admin.getId(), adminRole, entity.getId(), oldRole);
                throw new PermissionDeniedException("MANAGER는 ADMIN/MANAGER 권한을 부여할 수 없습니다.");
            }
        } else {
            log.info("[updateRoleType] 권한 변경 권한이 없는 사용자입니다. adminId={}, adminRole={}",
                    admin.getId(), adminRole);
            throw new PermissionDeniedException("권한 변경 권한이 없습니다.");
        }

        // 모든 검증을 통과한 경우 실제 권한 변경
        entity.setRole(d.getRole());
        memberRepository.save(entity);
        log.info("[updateRoleType] 권한 변경됨. id={}, username={}, role={} -> {}",
                entity.getId(), entity.getUsername(), oldRole, entity.getRole());
    }
}
