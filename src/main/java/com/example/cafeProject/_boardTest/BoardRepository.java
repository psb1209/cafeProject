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
    Optional<Board> findByCode(String code);

    @EntityGraph(attributePaths = "member") // Member 엔티티를 지연로딩 없이 바로 가져옴.
    Page<Board> findAll(Pageable pageable);

    /** 현재 읽기 권한 내에서 볼 수 있는 활성화된 모든 게시판을 출력 */
    @Query("""
        select b
        from Board b
        where b.enabled = true
          and b.readRole in :roles
    """)
    Page<Board> findVisible(@Param("roles") Collection<RoleType> roles,
                            Pageable pageable);

    /** 게시판 검색 */
    @Query("""
        select b
        from Board b
        where b.enabled = true
          and b.readRole in :roles
          and lower(b.name) like lower(concat('%', :keyword, '%'))
    """)
    Page<Board> searchVisible(@Param("roles") Collection<RoleType> roles,
                              @Param("keyword") String keyword,
                              Pageable pageable);

    /** 게시판 초성 검색 */
    @Query("""
        select b
        from Board b
        where b.enabled = true
          and b.readRole in :roles
          and b.nameKey like concat('%', :keyword, '%')
    """)
    Page<Board> searchVisibleByChosung(@Param("roles") Collection<RoleType> roles,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);

    boolean existsByCode(String code);
    boolean existsByName(String name);
}
