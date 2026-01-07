package com.example.cafeProject._cafeTest;

import com.example.cafeProject._boardTest.Board;
import com.example.cafeProject.member.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface CafeRepository extends JpaRepository<Cafe, Integer> {
    Optional<Cafe> findByCode(String code);

    Optional<Cafe> findByCodeAndEnabledTrue(String code);

    @EntityGraph(attributePaths = "member") // Member 엔티티를 지연로딩 없이 바로 가져옴.
    Page<Cafe> findAll(Pageable pageable);

    /** 현재 읽기 권한 내에서 볼 수 있는 활성화된 모든 카페를 출력 */
    @Query("""
        select c
        from Cafe c
        where c.enabled = true
    """)
    Page<Cafe> findVisible(Pageable pageable);

    /** 카페 검색 */
    @Query("""
        select c
        from Cafe c
        where c.enabled = true
          and lower(c.name) like lower(concat('%', :keyword, '%'))
    """)
    Page<Cafe> searchVisible(@Param("keyword") String keyword,
                             Pageable pageable);

    /** 카페 초성 검색 */
    @Query("""
        select c
        from Cafe c
        where c.enabled = true
          and c.nameKey like concat('%', :keyword, '%')
    """)
    Page<Cafe> searchVisibleByChosung(@Param("keyword") String keyword,
                                      Pageable pageable);

    boolean existsByCode(String code);
    boolean existsByName(String name);
}
