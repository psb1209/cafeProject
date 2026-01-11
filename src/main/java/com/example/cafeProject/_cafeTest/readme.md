# Cafe 모듈 (`_cafeTest`) - 카페 생성/목록/진입/메타

> 마지막 업데이트: 2026-01-11 (KST)

이 패키지는 이름만 `_cafeTest`일 뿐, **실제로는 카페(Cafe) 도메인의 정식 기능**입니다.  
초기에 테스트용으로 만들었다가 기능이 커지면서 정식 흐름에 편입되어 패키지명이 잔재처럼 남아 있습니다.

---

## 빠른 요약 (이 패키지가 하는 일)

- 카페 엔티티(`Cafe`) + DTO(`CafeDTO`) + 저장소(`CafeRepository`) + 서비스(`CafeService`) + 컨트롤러(`CafeController`) 구성
- **카페 목록**: 활성화(enabled=true)된 카페를 페이징/검색(초성 검색 포함)해서 보여줌
- **카페 진입**: `/cafe/{code}` → `/cafe/main?c={code}` 로 리다이렉트
- **카페 메인**: 공지/정보/커뮤니티 기본 게시판의 최신 글을 모아 보여줌
- **관리(운영진/관리자)**: 카페 생성/수정 + 메타 페이지 제공
- **생성 직후 기본 게시판 자동 생성**: `DefaultBoardProvisioner.ensureDefaults(cafe)` 호출로 기본 4개 게시판 자동 준비

---

## 라우팅(URL) 치트시트

> 기본 prefix: `/cafe` (`@RequestMapping("/cafe")`)

### 사용자/공개 영역
- `GET /cafe/cafeList`
    - 카페 목록(페이징/검색)
    - 파라미터: `keyword` (선택)
- `GET /cafe/{code}`
    - 카페 코드로 진입 → `redirect:/cafe/main?c={code}`
- `GET /cafe/main?c={code}`
    - 카페 메인 화면
    - 기본적으로 **공지/정보/커뮤니티** 게시판 최신 글을 보여줌
- (주의) `GET /cafe` / `GET /cafe/list`
    - `list()`를 404로 막아둔 상태라 접근하면 Not Found가 발생하도록 되어있음

### 운영진/관리자(권한 필요)
> `@ManagementOnly` = `hasAnyRole('ADMIN', 'MANAGER')`

- `GET /cafe/create`
- `POST /cafe/createProc`
- `GET /cafe/update/{id}`
- `POST /cafe/updateProc`
- `GET /cafe/meta/{id}`
    - 카페 코드 복사, 작성자/생성일 등 메타 확인용 화면

### 이미지 업로드(공통 기능)
- `POST /cafe/uploadImage`
    - `multipart/form-data`로 `file` 업로드
    - JSON 응답: `{ "url": "...", "fileName": "..." }`
    - 설정값:
        - `app.image.upload-dir` (저장 폴더)
        - `app.image.url-prefix` (클라이언트에 반환할 URL prefix)
    - `imageUrlPrefix`는 모든 뷰 모델에 자동 주입됨(BaseImageController)

---

## 데이터 모델(핵심 필드)

### `Cafe` (Entity) - `cafes` 테이블
- `name` : 카페 이름 (unique)
- `nameKey` : 초성 검색용 키 (name 기반으로 생성)
- `description` : 카페 설명
- `topic` : 카페 주제(짧은 태그 느낌)
- `imgName` : 업로드된 이미지 파일명
- `code` : 카페 코드(링크용, unique, 생성 후 변경 불가)
- `enabled` : 활성화 여부
- `member` : 작성자(운영자) 정보 (※ `BaseEntity`에 포함)

### `CafeDTO`
- 생성 시 검증
    - `name` 필수, 최대 100자
    - `description` 필수
    - `code` 필수(2~20), 정규식: `^[a-z0-9_-]+$`
- `normalize()`로 name trim / code 소문자 정규화

---

## 서비스 로직 포인트 (`CafeService`)

### 1) “보이는 카페” 규칙
- 일반 사용자/비로그인:
    - `enabled=true` 인 카페만 조회 가능
- 매니저/관리자:
    - 비활성 카페도 조회 가능 (점검/운영 목적)

### 2) 목록/검색
- 목록은 기본적으로 `enabled=true`만 노출
- `keyword` 검색 지원
    - 일반 문자열: `name like %keyword%`
    - 초성 검색: `nameKey like %초성깨진키%`

### 3) 생성 시 처리(중요)
- `beforeInsert()`
    - DTO 정규화 + 서버에서 강제 세팅(작성자/생성일 등 불필요 값 제거)
    - `enabled=true` 강제
    - `nameKey = BaseUtility.toChosungKey(name)` 자동 생성
    - `name/code` 중복 체크 후 예외 발생(필드 에러로 매핑됨)
- `afterInsert()`
    - `DefaultBoardProvisioner.ensureDefaults(cafe)` 실행
    - 기본 게시판 4개 자동 생성:
        - `communityBoard` / `informationBoard` / `noticeBoard` / `operationBoard`

### 4) 수정 시 처리
- `beforeUpdate()`
    - 수정 대상 `id` 필수
    - 생성일/작성자 관련 필드는 수정 불가(무시)
    - `imgName`이 비어있으면 기존 이미지 유지

### 5) 삭제 차단
- `beforeDelete()`에서 `METHOD_NOT_ALLOWED` 예외로 삭제 자체를 막아둠  
  (카페 삭제 플로우가 필요하면 “soft-delete(비활성화)”로 처리하는 방향)

---

## 관련 파일 위치

### Java
- `src/main/java/com/example/cafeProject/_cafeTest/`
    - `Cafe.java`
    - `CafeDTO.java`
    - `CafeRepository.java`
    - `CafeService.java`
    - `CafeController.java`

### Thymeleaf 템플릿
- `src/main/resources/templates/cafe/`
    - `list.html` : 카페 목록(검색/페이징)
    - `main.html` : 카페 메인(최신글 모아보기)
    - `meta.html` : 카페 메타(운영진)
    - `create.html` / `update.html` : 생성/수정(운영진)