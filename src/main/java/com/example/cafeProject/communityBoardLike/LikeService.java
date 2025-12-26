package com.example.cafeProject.communityBoardLike;


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
    public void createProc(LikeDTO likeDTO){

        //부모글있는지 판단후 있으면 좋아요 저장로직

        Optional<Like> optionalLike =likeRepository.findByCommunityBoardNumberAndUserId(likeDTO.getCommunityBoardNumber(), likeDTO.getUserId());
        if(optionalLike.isPresent()){
            Like like=optionalLike.get();
            likeRepository.delete(like);
        }else{
            Like like=new Like();
            like.setCommunityBoardNumber(likeDTO.getCommunityBoardNumber());
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

    public boolean isLike(int communityBoardNumber, int userId){
        return likeRepository.findByCommunityBoardNumberAndUserId(communityBoardNumber,userId).isPresent();
    }

}
