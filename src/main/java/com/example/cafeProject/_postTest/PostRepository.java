package com.example.cafeProject._postTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {

    /** 다른 연관관계에 있는 엔티티들을 지연 로딩 방식이 아닌 즉시 가져오는 findById */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        where p.id = :id
          and p.deleted = false
    """)
    Optional<Post> findDetailById(@Param("id") Integer id);

    /** Board 엔티티의 code 필드를 기준으로 찾는 페이지 리스트 */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        join p.board b
        where b.code = :code
          and p.deleted = false
    """)
    Page<Post> findByBoard_Code(@Param("code") String code, Pageable pageable);

    /** 소프트 삭제된 게시글 상세 보기 */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        where p.id = :id
          and p.deleted = true
    """)
    Optional<Post> findTrashById(@Param("id") Integer id);

    /** 소프트 삭제된 개시글들을 찾는 메서드 */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        join p.board b
        where b.code = :code
          and p.deleted = true
    """)
    Page<Post> findTrashByBoard_Code(@Param("code") String code, Pageable pageable);

    /** 게시글 검색 */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        join p.board b
        where b.code = :code
          and p.deleted = false
          and lower(p.title) like lower(concat('%', :keyword, '%'))
    """)
    Page<Post> searchByTitle(@Param("code") String code, @Param("keyword") String keyword, Pageable pageable);

    /** 게시글 초성 검색 */
    @EntityGraph(attributePaths = {"board", "member"})
    @Query("""
        select p
        from Post p
        join p.board b
        where b.code = :code
          and p.deleted = false
          and (
               p.titleKey like concat('%', :key, '%')
            or p.title like concat('%', :raw, '%')
          )
    """)
    Page<Post> searchByChosungTitle(@Param("code") String code, @Param("key") String key, @Param("raw") String raw, Pageable pageable);
}
