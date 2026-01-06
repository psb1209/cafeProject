package com.example.cafeProject._boardTest;

import com.example.base.BaseImageService;
import com.example.base.BaseUtility;
import com.example.cafeProject._cafeTest.Cafe;
import com.example.cafeProject._cafeTest.CafeService;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.member.RoleType;
import com.example.exception.DuplicateValueException;
import com.example.exception.EntityNotFoundException;
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
public class BoardService extends BaseImageService<Board, BoardDTO> {

    private final BoardRepository boardRepository;
    private final CafeService cafeService;
    private final MemberService memberService;

    public BoardService(BoardRepository repository, ModelMapper modelMapper, CafeService cafeService, MemberService memberService) {
        super(repository, modelMapper, Board.class, BoardDTO.class);
        this.boardRepository = repository;
        this.cafeService = cafeService;
        this.memberService = memberService;
    }

    public Board viewByCode(String cafeCode, String code) {
        return boardRepository.findByCafe_CodeAndCode(cafeCode, code)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시판 code=" + code));
    }
    public BoardDTO viewDTOByCode(String cafeCode, String code) {
        return toDTO(viewByCode(cafeCode, code));
    }

    public Page<Board> list(String cafeCode, Pageable pageable) {
        return boardRepository.findByCafe_Code(cafeCode, pageable);
    }
    public Page<BoardDTO> listDTO(String cafeCode, Pageable pageable) {
        return list(cafeCode, pageable).map(this::toDTO);
    }

    public Page<Board> listVisible(String cafeCode, Pageable pageable, RoleType[] roles) {
        List<RoleType> roleList = normalizeRoles(roles);
        if (roleList.isEmpty()) return Page.empty(pageable);
        return boardRepository.findVisible(cafeCode, roleList, pageable);
    }
    public Page<BoardDTO> listVisibleDTO(String cafeCode, Pageable pageable, RoleType[] roles) {
        return listVisible(cafeCode, pageable, roles).map(this::toDTO);
    }

    public Page<Board> listVisible(String cafeCode, Pageable pageable, RoleType[] roles, String keyword) {
        List<RoleType> roleList = normalizeRoles(roles);

        if (roleList.isEmpty())
            return Page.empty(pageable);

        if (keyword == null || keyword.isBlank())
            return boardRepository.findVisible(cafeCode, roleList, pageable);

        if (BaseUtility.isChosungQuery(keyword.trim()))
            return boardRepository.searchVisibleByChosung(cafeCode, roleList, BaseUtility.jaeumBreaker(keyword), pageable);

        return boardRepository.searchVisible(cafeCode, roleList, keyword.trim(), pageable);
    }
    public Page<BoardDTO> listVisibleDTO(String cafeCode, Pageable pageable, RoleType[] roles, String keyword) {
        return listVisible(cafeCode, pageable, roles, keyword).map(this::toDTO);
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
    public Board getReference(Integer id) {
        return boardRepository.getReferenceById(id);
    }


    @Override
    public Integer getIdFromDTO(BoardDTO dto) {
        return dto.getId(); // BaseDTO의 id 사용
    }

    @Override
    protected Board toEntity(BoardDTO dto) {
        Authentication authentication = memberService.getCurrentMember();
        if (memberService.isNotLogin(authentication)) throw new AccessDeniedException("현재 로그인 정보를 확인할 수 없습니다.");
        Board board = super.toEntity(dto);
        board.setMember(memberService.viewCurrentMember(authentication));
        board.setCafe(resolveCafe(dto));
        return board;
    }

    @Override
    protected BoardDTO toDTO(Board board) {
        BoardDTO dto = super.toDTO(board);
        if (board.getMember() != null) {
            dto.setMemberId(board.getMember().getId());
            dto.setUsername(board.getMember().getUsername());
        }
        return dto;
    }

    @Override
    protected void updateEntity(Board board, BoardDTO dto) {
        board.setDescription(dto.getDescription());
        board.setWriteRole(dto.getWriteRole());
        board.setReadRole(dto.getReadRole());
        board.setEnabled(dto.isEnabled());

        if (dto.getImgName() != null && !dto.getImgName().isBlank()) {
            board.setImgName(dto.getImgName());
        }
    }

    @Override
    protected void beforeInsert(BoardDTO dto) {
        dto.normalize();
        dto.setId(null);
        dto.setCreateDate(null);
        dto.setMemberId(null);
        dto.setUsername(null);
        dto.setEnabled(true);

        // 기본값
        if (dto.getReadRole() == null)  dto.setReadRole(RoleType.GUEST);
        if (dto.getWriteRole() == null) dto.setWriteRole(RoleType.USER);

        // 초성 검색을 위한 nameKey 세팅
        dto.setNameKey(BaseUtility.toChosungKey(dto.getName()));

        if (dto.getReadRole() == RoleType.BANNED || dto.getWriteRole() == RoleType.BANNED)
            throw new IllegalArgumentException("읽기/쓰기 권한에 BANNED는 사용할 수 없습니다.");

        // 읽기 권한은 쓰기 권한보다 높을 수 없음
        if (roleRank(dto.getReadRole()) > roleRank(dto.getWriteRole()))
            throw new IllegalArgumentException("읽기 권한은 쓰기 권한보다 높을 수 없습니다.");

        // 중복 체크
        if (boardRepository.existsByCafe_IdAndName(dto.getCafeId(), dto.getName()))
            throw new DuplicateValueException("이미 존재하는 게시판 이름입니다.", "name", dto.getName());
        if (boardRepository.existsByCafe_IdAndCode(dto.getCafeId(), dto.getCode()))
            throw new DuplicateValueException("이미 사용 중인 게시판 코드입니다.", "code", dto.getCode());
    }

    @Override
    protected void beforeUpdate(BoardDTO dto, Board board) {
        if (dto.getId() == null) throw new IllegalArgumentException("수정 대상 ID가 없습니다.");

        dto.setCreateDate(null);
        dto.setMemberId(null);
        dto.setUsername(null);
        dto.setCafeId(null);
        dto.setCafeCode(null);

        if (dto.getImgName() == null || dto.getImgName().isBlank()) dto.setImgName(board.getImgName());
        if (dto.getReadRole() == null)  dto.setReadRole(board.getReadRole());
        if (dto.getWriteRole() == null) dto.setWriteRole(board.getWriteRole());

        if (dto.getReadRole() == RoleType.BANNED || dto.getWriteRole() == RoleType.BANNED)
            throw new IllegalArgumentException("읽기/쓰기 권한에 BANNED는 사용할 수 없습니다.");

        // 읽기 권한은 쓰기 권한보다 높을 수 없음
        if (roleRank(dto.getReadRole()) > roleRank(dto.getWriteRole()))
            throw new IllegalArgumentException("읽기 권한은 쓰기 권한보다 높을 수 없습니다.");
    }

    /** 모종의 방법으로 delete 메서드 접근시 차단 */
    @Override
    protected void beforeDelete(Board board) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "게시판 삭제는 허용되지 않습니다.");
    }

    /** dto에서 Cafe를 추출, 기본은 reference지만 id가 없다면 code기반 view */
    private Cafe resolveCafe(BoardDTO dto) {
        if (dto.getCafeId() != null)
            return cafeService.getReference(dto.getCafeId());
        if (dto.getCafeCode() != null && !dto.getCafeCode().isBlank())
            return cafeService.viewByCode(dto.getCafeCode());

        throw new IllegalArgumentException("cafeId/cafeCode가 없습니다.");
    }

    /** 배열로 들어온 RoleType을 리스트로 변환 */
    private List<RoleType> normalizeRoles(RoleType[] roles) {
        if (roles == null || roles.length == 0) return List.of();
        List<RoleType> roleList = new ArrayList<>(roles.length);
        for (RoleType r : roles) {
            if (r == null) continue;
            if (roleList.contains(r)) continue;
            roleList.add(r);
        }
        return roleList;
    }

    /** RoleType마다 점수를 부여해서 비교하기 쉽게 하는 메서드 */
    private int roleRank(RoleType role) {
        if (role == null) return Integer.MIN_VALUE;

        switch (role) {
            case GUEST:
                return 0;
            case USER:
                return 10;
            case MANAGER:
                return 20;
            case ADMIN:
                return 30;
            default:
                return Integer.MIN_VALUE;
        }
    }
}
