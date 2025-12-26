package com.example.cafeProject.communityBoardComment;


import com.example.cafeProject.communityBoard.CommunityBoard;
import com.example.cafeProject.communityBoard.CommunityBoardDTO;
import com.example.cafeProject.communityBoard.CommunityBoardRepository;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Service
public class CommunityBoardCommentService {
    private final CommunityBoardCommentRepository communityBoardCommentRepository;
    private final CommunityBoardRepository communityBoardRepository;
    private final MemberRepository memberRepository;

    public Page<CommunityBoardComment> communityBoardCommentPage(int communityBoardCommentId,Pageable pageable){
        return communityBoardCommentRepository.findByCommunityBoardIdOrderByRefDescLevelAsc(communityBoardCommentId,pageable);
    }


    public void setInsert(
            CommunityBoardCommentDTO communityBoardCommentDTO,
            UserDetails userDetails
    ){
        
        CommunityBoard communityBoard=null;
        Optional<CommunityBoard> communityBoardOptional=communityBoardRepository.findById(communityBoardCommentDTO.getCommunityBoardId());
        if(communityBoardOptional.isPresent()){
            communityBoard=communityBoardOptional.get(); //부모글 존재유무판단
        }

        Member member=memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원 없음"));


        //새로운 댓글 엔티티 객체 생성해서 담은후 댓글 db저장
        CommunityBoardComment communityBoardComment=new CommunityBoardComment();
        int ref = communityBoardCommentRepository.getMaxRef()+1;
        communityBoardComment.setContent(communityBoardCommentDTO.getContent());
        communityBoardComment.setCommunityBoard(communityBoard);
        communityBoardComment.setRef(ref); //새댓글 +1
        communityBoardComment.setStep(0);   //새댓글 기본값 0
        communityBoardComment.setLevel(0);  //새댓글 기본값 0
        communityBoardComment.setMember(member);
        communityBoardCommentRepository.save(communityBoardComment);

    }

    public CommunityBoardComment getSelectOneById(int id){
        Optional<CommunityBoardComment> communityBoardCommentOptional =communityBoardCommentRepository.findById(id);
        CommunityBoardComment communityBoardComment=null;
        if(communityBoardCommentOptional.isPresent()){
            communityBoardComment =communityBoardCommentOptional.get();
        }
        return communityBoardComment;
    }

    public void setDelete(int commentId){
        CommunityBoardComment communityBoardComment=new CommunityBoardComment();
        communityBoardComment.setId(commentId);
        communityBoardCommentRepository.delete(communityBoardComment);
    }

    public void setDeleteAll(CommunityBoardDTO communityBoardDTO){
        List<CommunityBoardComment> communityBoardComment=communityBoardCommentRepository.findByCommunityBoardId(communityBoardDTO.getId());
        communityBoardCommentRepository.deleteAll(communityBoardComment);
    }

    @Transactional
    public CommunityBoardComment setUpdate(CommunityBoardCommentDTO communityBoardCommentDTO){
        CommunityBoardComment communityBoardComment =getSelectOneById(communityBoardCommentDTO.getId());

        communityBoardComment.setContent(communityBoardCommentDTO.getContent());

        return communityBoardComment;

    }

    //대댓글 추가
    @Transactional
    public void replysetInsert(
            CommunityBoardCommentDTO communityBoardCommentDTO,
            UserDetails userDetails
    ){
        CommunityBoardComment communityBoardComment_=communityBoardCommentRepository.findById(communityBoardCommentDTO.getCommunityBoardCommentId())
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));

        Member member=memberRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원 없음"));

        communityBoardCommentRepository.updateRelevel(
                communityBoardComment_.getRef(),
                communityBoardComment_.getLevel()
        );

        int ref=communityBoardComment_.getRef();
        int step=communityBoardComment_.getStep()+1;
        int level=communityBoardComment_.getLevel()+1;

        CommunityBoard communityBoard=null;
        Optional<CommunityBoard> communityBoardOptional=communityBoardRepository.findById(communityBoardCommentDTO.getCommunityBoardId());
        if(communityBoardOptional.isPresent()){
            communityBoard=communityBoardOptional.get(); //부모글 존재유무판단
        }


        CommunityBoardComment communityBoardComment=new CommunityBoardComment();
        communityBoardComment.setContent(communityBoardCommentDTO.getContent());
        communityBoardComment.setCommunityBoard(communityBoard);
        communityBoardComment.setMember(member);
        communityBoardComment.setRef(ref);
        communityBoardComment.setStep(step);
        communityBoardComment.setLevel(level);

        communityBoardCommentRepository.save(communityBoardComment);
    }
}
