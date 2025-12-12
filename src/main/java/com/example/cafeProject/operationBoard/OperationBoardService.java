package com.example.cafeProject.operationBoard;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class OperationBoardService {

    private final OperationBoardRepository operationBoardRepository;

    public Page<OperationBoard> list(Pageable pageable) {
        return operationBoardRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public OperationBoard getSelectOneById(OperationBoardDTO paramDTO) {
        return operationBoardRepository.findById(paramDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 ID(" + paramDTO.getId() + ")를 불러올 수 없습니다."));
    }

    @Transactional
    public void setInsert(OperationBoardDTO paramDTO) {
        OperationBoard operationBoard = new OperationBoard();
        operationBoard.setSubject(paramDTO.getSubject());
        operationBoard.setContent(paramDTO.getContent());
        operationBoard.setCnt(0);

        operationBoardRepository.save(operationBoard);
    }

    @Transactional
    public OperationBoard setUpdate(OperationBoardDTO paramDTO) {
        OperationBoard operationBoard = getSelectOneById(paramDTO);

        operationBoard.setSubject(paramDTO.getSubject());
        operationBoard.setContent(paramDTO.getContent());
        /*operationBoard.setCnt(paramDTO.getCnt());*/

        return operationBoard;
    }

    @Transactional
    public void setDelete(OperationBoardDTO paramDTO) {
        OperationBoard operationBoard = getSelectOneById(paramDTO);

        // 1. 메모 내용에서 이미지 src 추출
        List<String> imageUrls = extractImageUrls(operationBoard.getContent());

        // 2. 이미지 파일 삭제
        deleteImageFiles(imageUrls);

        operationBoardRepository.delete(operationBoard);
    }

    //************************************************************************************************************************
    //서머노트관련
    private List<String> extractImageUrls(String html) {
        List<String> urls = new ArrayList<>();

        Document doc = Jsoup.parse(html);
        Elements imgs = doc.select("img");

        for (Element img : imgs) {
            String src = img.attr("src");
            urls.add(src);
        }

        return urls;
    }

    private void deleteImageFiles(List<String> imageUrls) {
        String basePath = "C:/dw202/attach/summernote/"; // 파일 위치 변경

        for (String url : imageUrls) {
            try {
                // URL에서 파일명만 가져오기
                String fileName = Paths.get(url).getFileName().toString();

                File file = new File(basePath + fileName);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //************************************************************************************************************************


}
