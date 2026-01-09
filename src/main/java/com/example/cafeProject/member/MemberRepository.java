package com.example.cafeProject.member;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    // 활성(미삭제) 회원만 조회/체크
    Optional<Member> findByUsernameAndDeletedFalse(String username);
    Optional<Member> findByIdAndDeletedFalse(Integer id);
    Page<Member> findAllByDeletedFalse(Pageable pageable);

    boolean existsByUsernameAndDeletedFalse(String username);
    boolean existsByEmailAndDeletedFalse(String email);

    // 필요하면 "삭제 포함" 조회용
    Optional<Member> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // 삭제(탈퇴) 회원 조회용
    Page<Member> findAllByDeletedTrue(Pageable pageable);
    Page<Member> findAllByDeletedTrueAndDeleteReason(ReasonType deleteReason, Pageable pageable);

    interface WithdrawalReasonCount {
        ReasonType getReason();
        long getCnt();
    }

    @Query("""
            select m.deleteReason as reason, count(m) as cnt
            from Member m
            where m.deleted = true
            group by m.deleteReason
            order by cnt desc
            """)
    List<WithdrawalReasonCount> countDeletedByReason();
}
