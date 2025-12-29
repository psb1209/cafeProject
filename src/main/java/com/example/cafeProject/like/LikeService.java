package com.example.cafeProject.like;


import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createProc(LikeDTO likeDTO) {
        Optional<Like> optionalLike = Optional.empty();

        if (likeDTO.getCommunityBoardNumber() != null)
            optionalLike = likeRepository.findByCommunityBoardNumberAndUserId(likeDTO.getCommunityBoardNumber(), likeDTO.getUserId());
        if (likeDTO.getNoticeBoardNumber() != null)
            optionalLike = likeRepository.findByNoticeBoardNumberAndUserId(likeDTO.getNoticeBoardNumber(), likeDTO.getUserId());
        if (likeDTO.getInformationBoardNumber() != null)
            optionalLike = likeRepository.findByInformationBoardNumberAndUserId(likeDTO.getInformationBoardNumber(), likeDTO.getUserId());
        if (likeDTO.getOperationBoardNumber() != null)
            optionalLike = likeRepository.findByOperationBoardNumberAndUserId(likeDTO.getOperationBoardNumber(), likeDTO.getUserId());

        if (optionalLike.isPresent()) {
            likeRepository.delete(optionalLike.get());
        } else {
            Like like = new Like();
            like.setCommunityBoardNumber(likeDTO.getCommunityBoardNumber());
            like.setNoticeBoardNumber(likeDTO.getNoticeBoardNumber());
            like.setInformationBoardNumber(likeDTO.getInformationBoardNumber());
            like.setOperationBoardNumber(likeDTO.getOperationBoardNumber());
            like.setUserId(likeDTO.getUserId());
            likeRepository.save(like);
        }
    }

    public Like selectOneById(int id){
        return likeRepository.findById(id).orElseThrow();
    }

    public Member selectByUsername(String username){
        return memberRepository.findByUsername(username).orElseThrow();
    }

    public boolean isLike(Integer boardNumber, int userId) {
        if (boardNumber == null) return false;
        return likeRepository.findByCommunityBoardNumberAndUserId(boardNumber, userId).isPresent()
                || likeRepository.findByNoticeBoardNumberAndUserId(boardNumber, userId).isPresent()
                || likeRepository.findByInformationBoardNumberAndUserId(boardNumber, userId).isPresent()
                || likeRepository.findByOperationBoardNumberAndUserId(boardNumber, userId).isPresent();
    }

    public int likeCnt(String boardCode, int postId) {
        if (boardCode == null || boardCode.isBlank()) return 0;

        if (boardCode.toLowerCase().contains("community"))
            return likeRepository.countLikeWithCommunityBoardNumber(postId);
        if (boardCode.toLowerCase().contains("notice"))
            return likeRepository.countLikeWithNoticeBoardNumber(postId);
        if (boardCode.toLowerCase().contains("information"))
            return likeRepository.countLikeWithInformationBoardNumber(postId);
        if (boardCode.toLowerCase().contains("operation"))
            return likeRepository.countLikeWithOperationBoardNumber(postId);

        return 0;
    }

}
