package com.example.cafeProject.like;


import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        if (likeDTO.getPostId() != null)
            optionalLike = likeRepository.findByPostIdAndUserId(likeDTO.getPostId(), likeDTO.getUserId());

        if (optionalLike.isPresent()) {
            likeRepository.delete(optionalLike.get());
        } else {
            Like like = new Like();
            like.setCommunityBoardNumber(likeDTO.getCommunityBoardNumber());
            like.setNoticeBoardNumber(likeDTO.getNoticeBoardNumber());
            like.setInformationBoardNumber(likeDTO.getInformationBoardNumber());
            like.setOperationBoardNumber(likeDTO.getOperationBoardNumber());
            like.setPostId(likeDTO.getPostId());
            like.setUserId(likeDTO.getUserId());
            likeRepository.save(like);
        }
    }

    public Like selectOneById(int id){
        return likeRepository.findById(id).orElseThrow();
    }

    public boolean isLike(String boardKey, Integer boardNumber, int userId) {
        if (boardNumber == null) return false;
        if (boardKey == null || boardKey.isBlank()) return false;

        String key = boardKey.toLowerCase();

        if (key.contains("community"))
            return likeRepository.findByCommunityBoardNumberAndUserId(boardNumber, userId).isPresent();
        if (key.contains("notice"))
            return likeRepository.findByNoticeBoardNumberAndUserId(boardNumber, userId).isPresent();
        if (key.contains("information"))
            return likeRepository.findByInformationBoardNumberAndUserId(boardNumber, userId).isPresent();
        if (key.contains("operation"))
            return likeRepository.findByOperationBoardNumberAndUserId(boardNumber, userId).isPresent();
        if (key.contains("post"))
            return likeRepository.findByPostIdAndUserId(boardNumber, userId).isPresent();

        return false;
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
        if (boardCode.toLowerCase().contains("post"))
            return likeRepository.countLikeWithPostId(postId);

        return 0;
    }

}
