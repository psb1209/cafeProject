package com.example.base;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseImageController<E, D> extends BaseCrudController<E, D> {

    @Value("${app.image.upload-dir}")
    protected String uploadDir; // 저장할 폴더

    @Value("${app.image.url-prefix}")
    protected String urlPrefix; // 클라이언트에 반환할 URL

    protected BaseImageController(BaseImageService<E, D> service, String basePath) {
        super(service, basePath);
    }

    /** 모든 링크에 자동으로 imageUrlPrefix를 모델에 담아서 보냄 */
    @ModelAttribute("imageUrlPrefix")
    public String imageUrlPrefix() {
        // /attach/summernote/ 형태로 끝에 / 보정
        return urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
    }

    @ResponseBody
    @PostMapping(value = "/uploadImage", produces = "application/json")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty())
            throw new IOException("업로드 파일이 비어있습니다.");

        File folder = new File(uploadDir);

        if (folder.exists() && !folder.isDirectory())
            throw new IOException("업로드 경로가 폴더가 아닙니다: " + folder.getAbsolutePath());
        if (!folder.exists() && !folder.mkdirs())
            throw new IOException("업로드 폴더 생성 실패: " + folder.getAbsolutePath());

        // 파일명에서 한글/특수문자 제거(안전하게)
        String original = Objects.requireNonNull(file.getOriginalFilename(), "파일 이름이 null입니다.")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = UUID.randomUUID() + "_" + original;

        file.transferTo(Paths.get(uploadDir, fileName).toFile()); // 파일 저장

        Map<String, Object> response = new HashMap<>();
        response.put("url", urlPrefix.endsWith("/") ? urlPrefix + fileName : urlPrefix + "/" + fileName);
        response.put("fileName", fileName);
        return response;
    }
}
