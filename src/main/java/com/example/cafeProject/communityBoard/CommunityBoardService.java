package com.example.cafeProject.communityBoard;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommunityBoardService {

    private final CommunityBoardRepository communityBoardRepository;

    @Transactional(readOnly = true)
    public Page<CommunityBoard> getSelectAllPage(Pageable pageable){
        return communityBoardRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public CommunityBoard getSelectOneById(int id){
        return communityBoardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당ID(" + id + ")의 레코드를 찾을 수 없습니다."));
    }

    @Transactional
    public void setInsert(
            CommunityBoardDTO communityBoardDTO
    ){
        CommunityBoard communityBoard=new CommunityBoard();
        communityBoard.setSubject(communityBoardDTO.getSubject());
        communityBoard.setContent(communityBoardDTO.getContent());
//        communityBoard.setMember(member);
        communityBoardRepository.save(communityBoard);

    }

    @Transactional
    public CommunityBoard setUpdate(
            CommunityBoardDTO communityBoardDTO
    ){
        CommunityBoard communityBoard =getSelectOneById(communityBoardDTO.getId());

        communityBoard.setSubject(communityBoardDTO.getSubject());
        communityBoard.setContent(communityBoardDTO.getContent());

        return communityBoard;

    }

    @Transactional
    public void setDelete(int id){
        CommunityBoard communityBoard=new CommunityBoard();
        communityBoard.setId(id);
        communityBoardRepository.delete(communityBoard);
    }


    void cntProc(CommunityBoard communityBoard){

        communityBoard.setCnt(communityBoard.getCnt()+1);
        communityBoardRepository.save(communityBoard);
    }



}
