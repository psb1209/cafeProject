package com.example.cafeProject._cafeTest;

import com.example.base.BaseImageService;
import com.example.base.BaseUtility;
import com.example.cafeProject._boardTest.DefaultBoardProvisioner;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.member.RoleType;
import com.example.exception.DuplicateValueException;
import com.example.exception.EntityNotFoundException;
import com.example.exception.PermissionDeniedException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class CafeService extends BaseImageService<Cafe, CafeDTO> {

    private final CafeRepository cafeRepository;
    private final MemberService memberService;
    private final DefaultBoardProvisioner provisioner;

    public CafeService(
            CafeRepository repository,
            ModelMapper modelMapper,
            MemberService memberService,
            DefaultBoardProvisioner provisioner) {
        super(repository, modelMapper, Cafe.class, CafeDTO.class);
        this.cafeRepository = repository;
        this.memberService = memberService;
        this.provisioner = provisioner;
    }

    public Cafe viewByCode(String code) {
        return cafeRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 카페 code=" + code));
    }
    public CafeDTO viewDTOByCode(String code) {
        return toDTO(viewByCode(code));
    }

    public Cafe viewVisibleByCode(String code) {
        RoleType[] roles = memberService.getEffectiveRoles(memberService.getCurrentMember());
        // 매니저/관리자는 비활성 카페도 접근 허용(운영/점검용)
        if (isManagerOrAbove(roles)) return viewByCode(code);

        // 일반 사용자/비로그인: enabled=true인 카페만
        return cafeRepository.findByCodeAndEnabledTrue(code)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않거나 비활성화된 카페 code=" + code));
    }
    public CafeDTO viewVisibleDTOByCode(String code) {
        return toDTO(viewVisibleByCode(code));
    }

    public Page<Cafe> listVisible(Pageable pageable) {
        return cafeRepository.findVisible(pageable);
    }
    public Page<CafeDTO> listVisibleDTO(Pageable pageable, RoleType[] roles) {
        return listVisible(pageable).map(this::toDTO);
    }

    public Page<Cafe> listVisible(Pageable pageable, String keyword) {
        if (keyword == null || keyword.isBlank())
            return cafeRepository.findVisible(pageable);

        if (BaseUtility.isChosungQuery(keyword.trim()))
            return cafeRepository.searchVisibleByChosung(BaseUtility.jaeumBreaker(keyword), pageable);

        return cafeRepository.searchVisible(keyword.trim(), pageable);
    }
    public Page<CafeDTO> listVisibleDTO(Pageable pageable, String keyword) {
        return listVisible(pageable, keyword).map(this::toDTO);
    }

    /**
     * 연관관계 연결용 "가짜 엔티티(프록시)"를 가져온다.
     * - findById(id) 처럼 DB를 조회해서 값을 가져오는 게 아니라,
     *   "id = ?" 인 Board가 있다고 가정하고 그 Board를 가리키는 대리 객체(proxy)를 만들어 반환한다.
     * - 따라서 이 메서드 호출 자체로는 보통 SELECT가 나가지 않는다. (쿼리 절약용)
     * - 단, 반환된 객체에서 name/description 같은 "id 외의 필드"를 실제로 읽는 순간
     *   그때 DB를 조회(SELECT)해서 값을 채울 수 있다. (지연 로딩처럼 동작)
     * - 주의:
     *   1) DB에 해당 id가 실제로 없으면, 프록시를 초기화하려는 시점에 예외가 날 수 있다.
     *   2) 트랜잭션 밖에서 프록시의 필드를 읽으면 LazyInitializationException이 날 수 있다.
     */
    public Cafe getReference(Integer id) {
        return cafeRepository.getReferenceById(id);
    }


    @Override
    public Integer getIdFromDTO(CafeDTO dto) {
        return dto.getId(); // BaseDTO의 id 사용
    }

    @Override
    protected Cafe toEntity(CafeDTO dto) {
        Authentication authentication = memberService.getCurrentMember();
        if (memberService.isNotLogin(authentication)) throw new AccessDeniedException("현재 로그인 정보를 확인할 수 없습니다.");
        Cafe cafe = super.toEntity(dto);
        cafe.setMember(memberService.viewCurrentMember(authentication));
        return cafe;
    }

    @Override
    protected CafeDTO toDTO(Cafe cafe) {
        CafeDTO dto = super.toDTO(cafe);
        if (cafe.getMember() != null) {
            dto.setMemberId(cafe.getMember().getId());
            dto.setUsername(cafe.getMember().getUsername());
        }
        return dto;
    }

    @Override
    protected void updateEntity(Cafe cafe, CafeDTO dto) {
        cafe.setDescription(dto.getDescription());
        cafe.setTopic(dto.getTopic());
        cafe.setEnabled(dto.isEnabled());

        if (dto.getImgName() != null && !dto.getImgName().isBlank()) {
            cafe.setImgName(dto.getImgName());
        }
    }

    @Override
    protected void beforeInsert(CafeDTO dto) {
        dto.normalize();
        dto.setId(null);
        dto.setCreateDate(null);
        dto.setMemberId(null);
        dto.setUsername(null);
        dto.setEnabled(true);

        // 초성 검색을 위한 nameKey 세팅
        dto.setNameKey(BaseUtility.toChosungKey(dto.getName()));

        // 중복 체크
        if (cafeRepository.existsByName(dto.getName()))
            throw new DuplicateValueException("이미 존재하는 게시판 이름입니다.", "name", dto.getName());
        if (cafeRepository.existsByCode(dto.getCode()))
            throw new DuplicateValueException("이미 사용 중인 게시판 코드입니다.", "code", dto.getCode());
    }

    @Override
    protected void afterInsert(CafeDTO dto, Cafe cafe) {
        provisioner.ensureDefaults(cafe);
    }

    @Override
    protected void beforeUpdate(CafeDTO dto, Cafe cafe) {
        if (dto.getId() == null) throw new IllegalArgumentException("수정 대상 ID가 없습니다.");

        dto.setCreateDate(null);
        dto.setMemberId(null);
        dto.setUsername(null);

        if (dto.getImgName() == null || dto.getImgName().isBlank()) dto.setImgName(cafe.getImgName());
    }

    /** 모종의 방법으로 delete 메서드 접근시 차단 */
    @Override
    protected void beforeDelete(Cafe board) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "게시판 삭제는 허용되지 않습니다.");
    }

    private boolean isManagerOrAbove(RoleType[] roles) {
        if (roles == null) return false;
        for (RoleType r : roles) {
            if (r == RoleType.ADMIN || r == RoleType.MANAGER) return true;
        }
        return false;
    }
}
