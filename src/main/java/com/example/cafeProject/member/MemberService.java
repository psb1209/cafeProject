package com.example.cafeProject.member;

import com.example.exception.EntityNotFoundException;
import com.example.exception.WrongPasswordException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private static final Class<MemberService> memberServiceClass = MemberService.class;
    private static final Logger log = LoggerFactory.getLogger(memberServiceClass);
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 전체 목록
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

    // 상세보기 (id로)
    public Member view(int id) throws EntityNotFoundException {
        log.debug("[{}] Entity 조회 시도, id={}", memberServiceClass.getSimpleName(), id);
        Member entity = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 id=" + id));
        log.debug("[{}] Entity 조회 성공, id={}", memberServiceClass.getSimpleName(), id);
        return entity;
    }

    // Optional을 반환하는 view 메서드
    public Optional<Member> viewOptional(int id) {
        log.debug("[{}] Optional 조회 시도, id={}", memberServiceClass.getSimpleName(), id);
        Optional<Member> result = memberRepository.findById(id);
        log.debug("[{}] Optional 조회 결과, id={}, present={}",
                memberServiceClass.getSimpleName(), id, result.isPresent());
        return result;
    }

    public Member viewCurrentMember(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("현재 로그인 정보를 확인할 수 없습니다.");
        }
        log.debug("[{}] currentMember 조회 시도, name={}", memberServiceClass.getSimpleName(), authentication.getName());
        Member entity = memberRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 username=" + authentication.getName()));
        log.debug("[{}] currentMember 조회 성공, name={}", memberServiceClass.getSimpleName(), authentication.getName());
        return entity;
    }

    // 등록
    @Transactional
    public Member setInsert(MemberDTO dto) {
        beforeCreate(dto);
        Member entity = toEntity(dto);
        afterCreate(dto, entity);
        return memberRepository.save(entity);
    }

    // 수정
    @Transactional
    public Member setUpdate(Authentication authentication, PasswordChangeDTO dto) {
        Member entity = viewCurrentMember(authentication);
        beforeUpdate(dto, entity);
        updateEntity(entity, dto);
        afterUpdate(dto, entity);
        return memberRepository.save(entity);
    }

    // 삭제
    @Transactional
    public void setDelete(Authentication authentication, MemberDeleteDTO dto) {
        Member entity = viewCurrentMember(authentication);
        beforeDelete(entity, dto);
        memberRepository.delete(entity);
        afterDelete(entity, dto);
    }

    // DTO → new Entity
    private Member toEntity(MemberDTO dto) {
        return Member.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .role(dto.getRole())
                .build();
    }
    // 기존 Entity + DTO로 필드 갱신
    private void updateEntity(Member e, PasswordChangeDTO d) {
        if (d.getNewPassword() != null && !d.getNewPassword().isBlank()) {
            e.setPassword(passwordEncoder.encode(d.getNewPassword()));
        }
    }

    // 이하의 메서드는 각 메서드의 기본 동작에 더해 추가 동작을 정의할 때 사용하는 메서드입니다.
    private void beforeCreate(MemberDTO dto) {
        dto.normalize();

        log.info("회원가입 시도: username={}, email={}",
                dto.getUsername(), dto.getEmail());

        if (memberRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 id");
        }
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 email");
        }

        if (dto.getRole() == null) {
            dto.setRole(RoleType.USER);
        }
    }
    private void afterCreate(MemberDTO dto, Member entity) {
        log.info("회원가입 완료: id={}, username={}, role={}",
                entity.getId(), entity.getUsername(), entity.getRole());
    }
    private void beforeUpdate(PasswordChangeDTO dto, Member entity) {
        log.info("회원 정보 수정 시도: id={}, username={}",
                entity.getId(), entity.getUsername());
        checkPassword(dto.getCurrentPassword(), entity);
    }
    private void afterUpdate(PasswordChangeDTO dto, Member entity) {
        log.info("회원 정보 수정 완료: id={}, username={}",
                entity.getId(), entity.getUsername());
    }
    private void beforeDelete(Member entity, MemberDeleteDTO dto) {
        checkPassword(dto.getPassword(), entity);
        log.warn("회원 삭제 시도: id={}, username={}",
                entity.getId(), entity.getUsername());
    }
    private void afterDelete(Member entity, MemberDeleteDTO dto) {
        log.warn("회원 삭제 완료: id={}, username={}",
                entity.getId(), entity.getUsername());
    }

    private void checkPassword(String password, Member entity) throws WrongPasswordException {
        if (!passwordEncoder.matches(password, entity.getPassword())) {
            throw new WrongPasswordException("올바르지 않은 현재 비밀번호");
        }
    }

    @Transactional
    public void updateRoleType(MemberDTO d, Member admin) {
        Member entity = view(d.getId());

        RoleType oldRole = entity.getRole();
        RoleType newRole = d.getRole();
        RoleType adminRole = admin.getRole();

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
                throw new IllegalArgumentException("ADMIN의 권한은 변경할 수 없습니다.");
            }
            if (newRole == RoleType.ADMIN) {
                log.info("[updateRoleType] ADMIN으로 권한을 변경할 수 없습니다. targetId={}, targetUsername={}",
                        entity.getId(), entity.getUsername());
                throw new IllegalArgumentException("ADMIN으로 권한을 변경할 수 없습니다.");
            }
        } else if (adminRole == RoleType.MANAGER) {
            if (oldRole == RoleType.ADMIN || oldRole == RoleType.MANAGER) {
                log.info("[updateRoleType] MANAGER는 ADMIN/MANAGER 계정의 권한을 변경할 수 없습니다. adminId={}, adminRole={}, targetId={}, targetRole={}",
                        admin.getId(), adminRole, entity.getId(), oldRole);
                throw new IllegalArgumentException("MANAGER는 ADMIN/MANAGER 계정의 권한을 변경할 수 없습니다.");
            }
            if (newRole == RoleType.ADMIN || newRole == RoleType.MANAGER) {
                log.info("[updateRoleType] MANAGER는 ADMIN/MANAGER 권한을 부여할 수 없습니다. adminId={}, adminRole={}, targetId={}, targetRole={}",
                        admin.getId(), adminRole, entity.getId(), oldRole);
                throw new IllegalArgumentException("MANAGER는 ADMIN/MANAGER 권한을 부여할 수 없습니다.");
            }
        } else {
            log.info("[updateRoleType] 권한 변경 권한이 없는 사용자입니다. adminId={}, adminRole={}",
                    admin.getId(), adminRole);
            throw new IllegalArgumentException("권한 변경 권한이 없습니다.");
        }
        entity.setRole(d.getRole());
        memberRepository.save(entity);
        log.info("[updateRoleType] 권한 변경됨. id={}, username={}, role={} -> {}",
                entity.getId(), entity.getUsername(), oldRole, entity.getRole());
    }
}
