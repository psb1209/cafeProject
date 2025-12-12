package com.example.cafeProject.communityBoardComment;


import com.example.cafeProject.communityBoard.CommunityBoard;
import com.example.cafeProject.communityBoard.CommunityBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@RequiredArgsConstructor
@Service
public class CommunityBoardCommentService {
    private final CommunityBoardCommentRepository communityBoardCommentRepository;
    private final CommunityBoardRepository communityBoardRepository;


    public Page<CommunityBoardComment> communityBoardCommentPage(int communityBoardCommentId,Pageable pageable){
        return communityBoardCommentRepository.findByCommunityBoardId(communityBoardCommentId,pageable);
    }


    public void setInsert(
            CommunityBoardCommentDTO communityBoardCommentDTO
    ){

        CommunityBoard communityBoard=null;
        Optional<CommunityBoard> communityBoardOptional=communityBoardRepository.findById(communityBoardCommentDTO.getCommunityBoardId());
        if(communityBoardOptional.isPresent()){
            communityBoard=communityBoardOptional.get(); //부모글 존재유무판단
        }

        //새로운 댓글 엔티티 객체 생성해서 담은후 댓글 db저장
        CommunityBoardComment communityBoardComment=new CommunityBoardComment();
        communityBoardComment.setContent(communityBoardCommentDTO.getContent());
        communityBoardComment.setCommunityBoard(communityBoard);
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

    public void setDelete(int commentid){
        CommunityBoardComment communityBoardComment=new CommunityBoardComment();
        communityBoardComment.setId(commentid);
        communityBoardCommentRepository.delete(communityBoardComment);
    }
}
