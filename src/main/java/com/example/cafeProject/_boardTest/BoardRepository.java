package com.example.cafeProject._boardTest;

import com.example.cafeProject.member.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    Optional<Board> findByCafe_CodeAndCode(String cafeCode, String code);

    @EntityGraph(attributePaths = "member") // Member 엔티티를 지연로딩 없이 바로 가져옴.
    Page<Board> findByCafe_Code(String cafeCode, Pageable pageable);

    @EntityGraph(attributePaths = "cafe")
    Optional<Board> findByCafe_CodeAndId(String cafeCode, Integer id);

    /** 현재 읽기 권한 내에서 볼 수 있는 활성화된 모든 게시판을 출력 */
    @Query("""
        select b
        from Board b
        join b.cafe c
        where b.enabled = true
          and c.code = :cafeCode
          and b.readRole in :roles
    """)
    Page<Board> findVisible(@Param("cafeCode") String cafeCode,
                            @Param("roles") Collection<RoleType> roles,
                            Pageable pageable);

    /** 게시판 검색 */
    @Query("""
        select b
        from Board b
        join b.cafe c
        where b.enabled = true
          and c.code = :cafeCode
          and b.readRole in :roles
          and lower(b.name) like lower(concat('%', :keyword, '%'))
    """)
    Page<Board> searchVisible(@Param("cafeCode") String cafeCode,
                              @Param("roles") Collection<RoleType> roles,
                              @Param("keyword") String keyword,
                              Pageable pageable);

    /** 게시판 초성 검색 */
    @Query("""
        select b
        from Board b
        join b.cafe c
        where b.enabled = true
          and c.code = :cafeCode
          and b.readRole in :roles
          and b.nameKey like concat('%', :keyword, '%')
    """)
    Page<Board> searchVisibleByChosung(@Param("cafeCode") String cafeCode,
                                       @Param("roles") Collection<RoleType> roles,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);

    /** 비활성화된 보드 확인 */
    @Query("""
        select b
        from Board b
        join b.cafe c
        where b.enabled = false
          and c.code = :cafeCode
    """)
    Page<Board> findTrash(@Param("cafeCode") String cafeCode,
                          Pageable pageable);

    /** id가 특정 카페에 속하는지 검증/조회 */
    boolean existsByCafe_CodeAndId(String cafeCode, Integer id);

    boolean existsByCafe_IdAndCode(Integer cafeId, String code);
    boolean existsByCafe_IdAndName(Integer cafeId, String name);
}
