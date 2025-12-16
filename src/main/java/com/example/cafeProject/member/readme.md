## 1. 주요 기능

### 1.1 회원(일반 사용자)

- **회원가입**
    - 접근: 익명만 (`@PreAuthorize("isAnonymous()")`)
    - 검증: `MemberDTO` + `ValidationGroups.OnCreate`
        - username: 3~20자, 영문/숫자/`_`/`-`만
        - password: 8자 이상, 영문+숫자 포함, 허용 특수문자만
        - email: 이메일 형식
    - 처리: `MemberService#setInsert(dto)`
        - `dto.normalize()` (trim, email 소문자화)
        - 금지 username 차단(우회 방지 compact 검사 포함)
        - username/email 중복 체크
        - role **USER로 강제**
        - 비밀번호 `PasswordEncoder.encode()` 후 저장

- **로그인**
    - 화면: `GET /member/login`
    - 실패 안내: `GET /member/login/error` (loginError 플래그)

- **내 정보 조회**
    - 접근: 로그인만 (`@PreAuthorize("isAuthenticated()")`)
    - `GET /member/view`
    - `viewCurrentMember(authentication)`로 현재 username 기반 조회(익명/없으면 예외)

- **비밀번호 변경**
    - 접근: 로그인만
    - 화면: `GET /member/passwordUpdate`
    - 처리: `POST /member/passwordProc`
        - `PasswordChangeDTO` 검증 + `newPassword == confirmPassword` 수동 검사
        - 현재 비밀번호 `matches` 실패 → `WrongPasswordException`
        - 새 비밀번호 인코딩 후 반영

- **회원 탈퇴**
    - 접근: 로그인만
    - 화면: `GET /member/delete`
    - 처리: `POST /member/deleteProc`
        - `MemberDeleteDTO` 검증(비밀번호 규칙 + 동의 체크 `@AssertTrue`)
        - 비밀번호 `matches` 성공 시 `memberRepository.delete(entity)`

---

### 1.2 관리자(ADMIN / MANAGER)

- **회원 목록(페이징)**
    - 접근: `@ManagementOnly`
    - `GET /member/list` (`@PageableDefault size=10, sort=id desc`)
    - `memberService.list(pageable)` → `Page<Member>`

- **특정 회원 조회**
    - 접근: `@ManagementOnly`
    - `GET /member/view/{id}`

- **권한(Role) 변경**
    - 접근: `@ManagementOnly`
    - 화면: `GET /member/roleUpdate/{id}`
        - `roles`(선택가능 RoleType 목록) + `data`(id/role DTO) + `member`(표시용) 전달
    - 처리: `POST /member/roleProc` (`MemberDTO` + OnUpdate 검증)
    - 정책(서비스 강제, 요약):
        - 공통: 같은 권한 변경 불가 / 자기 자신 변경 불가
        - ADMIN: 다른 ADMIN 변경 불가 / 누구도 ADMIN으로 승격 불가
        - MANAGER: ADMIN/MANAGER 대상 변경 불가 / ADMIN/MANAGER 부여 불가

---

## 2. 권한 및 접근 제어 규칙

### 2.1 용어/전제
- **익명(Anonymous)**: 로그인하지 않은 사용자 → `isAnonymous()`
- **인증(Authenticated)**: 로그인한 사용자 → `isAuthenticated()`
- **관리자(Management)**: ADMIN 또는 MANAGER → `@ManagementOnly`

서비스 레벨 로그인 방어(`isNotLogin(authentication)`) 기준:
- `authentication == null`
- `!authentication.isAuthenticated()`
- `authentication instanceof AnonymousAuthenticationToken`

---

### 2.2 URL별 접근 규칙(요약)

- **익명만**
    - `GET  /member/create`
    - `POST /member/createProc`

- **로그인만**
    - `GET  /member/view`
    - `GET  /member/passwordUpdate`
    - `POST /member/passwordProc`
    - `GET  /member/delete`
    - `POST /member/deleteProc`

- **관리자만(ADMIN/MANAGER)**
    - `GET  /member/list`
    - `GET  /member/view/{id}`
    - `GET  /member/roleUpdate/{id}`
    - `POST /member/roleProc`

- **누구나**
    - `GET /member/login`
    - `GET /member/login/error`

---

### 2.3 BANNED 계정 처리 규칙
현재 로그인 사용자의 Authorities에 `ROLE_BANNED`가 포함되면 아래 기능을 차단한다.
- 비밀번호 변경(`POST /member/passwordProc`)
- 회원 탈퇴(`POST /member/deleteProc`)
- 차단 시: 메인으로 redirect

> 참고: BANNED를 “로그인 자체 차단”으로도 가능하지만, 본 프로젝트는 컨트롤러에서 민감 기능을 추가 차단한다.

---

### 2.4 권한(Role) 변경 정책(관리 기능 내부 규칙)
권한 변경은 `@ManagementOnly`로 1차 제한 후,
`MemberService#updateRoleType(dto, admin)`에서 2차로 정책을 강제한다.

- 공통: 같은 권한 변경 불가 / 자기 자신 변경 불가
- ADMIN: 다른 ADMIN 변경 불가 / ADMIN 승격 불가
- MANAGER: ADMIN/MANAGER 대상 변경 불가 / ADMIN/MANAGER 부여 불가

---

### 2.5 현재 사용자(Member) 조회 규칙
`MemberService#viewCurrentMember(authentication)` 사용:
- 인증 정보가 없거나 익명: `AccessDeniedException`
- username 조회 실패: `EntityNotFoundException`

컨트롤러의 `loadPathOrRedirect(...)`는 위 예외를 잡아 조회 실패 시 메인으로 redirect한다.

---

## 3. URL 라우팅

- 기본 prefix: `/member`
- 뷰 템플릿: `member/*` (컨트롤러 `basePath="member"` 사용)

### 3.1 GET (화면)

- `GET /member/create` → `member/create`
    - model: `data = new MemberDTO()`

- `GET /member/login` → `member/login`
    - model: `data = new MemberDTO()`

- `GET /member/login/error` → `member/login`
    - model: `data`, `loginError=true`

- `GET /member/view` → `member/view`
    - model: `data = (현재 로그인 사용자 Member)`

- `GET /member/passwordUpdate` → `member/passwordUpdate`
    - model: `data = new PasswordChangeDTO()`

- `GET /member/delete` → `member/delete`
    - model: `data = new MemberDeleteDTO()`

---

### 3.2 GET (관리자 화면)

- `GET /member/list` → `member/list`
    - model: `list = Page<Member>`

- `GET /member/view/{id}` → `member/view`
    - model: `data = (대상 Member)`

- `GET /member/roleUpdate/{id}` → `member/roleUpdate`
    - model:
        - `member = (대상 Member, 표시용)`
        - `data = (id+role만 세팅한 MemberDTO, 제출용)`
        - `roles = (선택 가능한 RoleType[])`
    - 역할 옵션 예:
        - ADMIN: `{BANNED, USER, MANAGER}`
        - MANAGER: `{BANNED, USER}`

---

### 3.3 POST (처리)

- `POST /member/createProc`
    - 입력: `MemberDTO`
    - 검증: `@Validated(OnCreate)`
    - 성공: `memberService.setInsert(dto)` → `redirect:/member/login`
    - 실패: 검증/중복 등 → `member/create` 재표시, 기타 예외 → 런타임 에러 페이지

- `POST /member/passwordProc`
    - 입력: `PasswordChangeDTO`
    - 검증: `@Valid` + (confirm 일치 수동검증)
    - 추가 차단: 로그인 방어 + `ROLE_BANNED` 차단
    - 성공: `memberService.setUpdate(authentication, dto)` → `redirect:/`
    - 실패: 검증/비번불일치 → `member/passwordUpdate`, 기타는 정책에 따라 redirect 또는 에러 페이지

- `POST /member/deleteProc`
    - 입력: `MemberDeleteDTO`
    - 검증: `@Valid` (confirm AssertTrue 포함)
    - 추가 차단: 로그인 방어 + `ROLE_BANNED` 차단
    - 성공: `memberService.setDelete(authentication, dto)` → `redirect:/`
    - 실패: 검증/비번불일치 → `member/delete`, 기타는 정책에 따라 redirect 또는 에러 페이지

- `POST /member/roleProc`
    - 입력: `MemberDTO(id, role)`
    - 검증: `@Validated(OnUpdate)`
    - 성공: `memberService.updateRoleType(dto, admin)` → `redirect:/member/view/{id}`
    - 실패: 정책 위반(IllegalArgument/PermissionDenied 등) → `member/roleUpdate` 재표시(roles/member 재주입), 기타는 redirect 또는 에러 페이지

---

### 3.4 모델 네이밍 규칙
- 폼 바인딩용 모델명: **항상 `"data"`**
- 상세 조회 화면: `"data"`에 `Member` 엔티티를 담아 `/view`와 `/view/{id}`가 동일 템플릿(`member/view`) 공유
- 권한 변경 화면: `"member"`(표시용) + `"data"`(제출용) + `"roles"`(옵션 목록)

---

## 4. Validation 규칙

프로젝트는 DTO 계층에서 Bean Validation(JSR-380)으로 1차 검증을 수행하고,
컨트롤러는 `@Valid` 또는 `@Validated(그룹)` + `BindingResult`로 결과를 처리한다.
(단, DB 상태/권한/비밀번호 일치 등은 서비스에서 추가 검증)

---

### 4.1 검증 적용 방식(요약)

- **회원가입**: `@Validated(ValidationGroups.OnCreate.class)` + `MemberDTO`
- **권한 변경(관리자)**: `@Validated(ValidationGroups.OnUpdate.class)` + `MemberDTO`
- **비밀번호 변경**: `@Valid` + `PasswordChangeDTO`
- **회원 탈퇴**: `@Valid` + `MemberDeleteDTO`

검증 실패 시:
- `bindingResult.hasErrors()`면 즉시 폼 화면으로 복귀
- 공통 메서드 `logValidationErrors(action, path, bindingResult)`로 필드 에러 로깅 후 재표시

---

### 4.2 MemberDTO (회원가입/권한변경 공용)

같은 DTO라도 “생성/수정”에서 필요한 필드가 달라
**ValidationGroups**로 검증 규칙을 분리한다.

#### 4.2.1 OnCreate (회원가입) 규칙

- **username**
    - `@NotBlank` : 필수
    - `@Size(min=3, max=20)` : 길이 제한
    - `@Pattern("^[a-zA-Z0-9_-]+$")` : 영문/숫자/`_`/`-`만 허용

- **password**
    - `@NotBlank`
    - `@Size(min=8)`
    - `@Pattern("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z0-9~!@#$%^&*_\\-+=]+$")`
        - 영문 1자 이상 + 숫자 1자 이상 포함
        - 허용 문자: 영문/숫자 + `~!@#$%^&*_-+=`

- **email**
    - `@NotBlank`
    - `@Email`

> 참고: 가입 시 role은 서비스에서 USER로 강제하므로 폼 입력 role은 최종 저장값에 영향 없음.

#### 4.2.2 OnUpdate (관리자 권한 변경) 규칙

- **id**
    - `@NotNull` : 대상 식별자 필수

- **role**
    - `@NotNull` : 변경할 권한 필수

#### 4.2.3 normalize()

`MemberDTO#normalize()`는 입력값 품질을 통일한다.
- username: trim()
- password: trim()
- email: trim() + toLowerCase()

---

### 4.3 PasswordChangeDTO (비밀번호 변경)

#### 4.3.1 필드 규칙
- **currentPassword**
    - `@NotBlank`

- **newPassword**
    - `@NotBlank`
    - `@Size(min=8)`
    - `@Pattern("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z0-9~!@#$%^&*_\\-+=]+$")`

- **confirmPassword**
    - `@NotBlank`

#### 4.3.2 컨트롤러 수동 검증(필드 일치)
Bean Validation만으로 “new/confirm 일치”를 단순하게 처리하기 어려워
컨트롤러에서 추가 검증한다.

- `newPassword.equals(confirmPassword)`가 아니면
    - `bindingResult.rejectValue("confirmPassword", "mismatch", "...")`

#### 4.3.3 normalize()
- current/new/confirm 모두 trim()

---

### 4.4 MemberDeleteDTO (회원 탈퇴)

#### 4.4.1 필드 규칙
- **password**
    - `@NotBlank`
    - `@Size(min=8)`
    - `@Pattern("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z0-9~!@#$%^&*_\\-+=]+$")`

- **confirm** (동의 체크박스)
    - `@AssertTrue` : 반드시 true

- **reason**
    - `ReasonType` (enum)
    - 현재는 선택 입력(필요 시 `@NotNull`로 필수화 가능)

#### 4.4.2 normalize()
- password trim()

---

### 4.5 서비스 레벨 검증(Bean Validation과 별개)

DTO 검증을 통과해도 다음은 서비스에서 추가 검증한다.

- **회원가입**
    - 금지 username 정책(`checkForbiddenUsername`)
    - username/email 중복 검사
    - role USER 강제

- **비밀번호 변경/회원 탈퇴**
    - `PasswordEncoder.matches(input, entity.getPassword())`
    - 실패 시 `WrongPasswordException`

- **관리자 권한 변경**
    - ADMIN/MANAGER 정책 위반 시 `PermissionDeniedException` 또는 `IllegalArgumentException`

---

## 5. 예외 및 실패 처리

예외는 크게 3가지로 나눠 처리한다.

- **Validation 실패**: 컨트롤러에서 `BindingResult`로 처리 → 동일 폼 재표시
- **비즈니스 예외**: 서비스에서 발생 → 컨트롤러에서 catch 후 폼 에러/redirect로 변환
- **시스템 예외**: 예측 불가 예외 → 공통 런타임 에러 페이지로 redirect

---

### 5.1 Validation(검증) 실패 처리

POST 요청에서 DTO 검증이 실패하면:
- `bindingResult.hasErrors()` 즉시 true → 폼 화면으로 복귀
- `logValidationErrors(action, path, bindingResult)`로 필드 에러 로그 출력
- 화면은 `th:errors`, `#fields.hasErrors()`로 사용자에게 표시 가능

적용 위치 예:
- 회원가입: `/member/createProc` → `member/create`
- 비밀번호 변경: `/member/passwordProc` → `member/passwordUpdate`
- 회원 탈퇴: `/member/deleteProc` → `member/delete`
- 권한 변경: `/member/roleProc` → `member/roleUpdate`

---

### 5.2 컨트롤러 레벨 “수동 검증” 실패

Bean Validation으로 애매한 규칙은 컨트롤러에서 직접 검증한다.

- 비밀번호 변경: `newPassword`와 `confirmPassword` 일치 여부
    - 불일치 시 `confirmPassword` 필드 에러로 추가하고 폼 재표시

---

### 5.3 서비스 계층(비즈니스) 예외 목록

서비스는 DB 상태/권한/비밀번호 일치 같은 런타임 조건을 검증하며,
실패 시 아래 예외로 의미를 구분한다.

- `EntityNotFoundException`
    - id/username 기반 조회 실패

- `AccessDeniedException`
    - 인증 정보가 없거나 익명으로 판단

- `WrongPasswordException`
    - `PasswordEncoder.matches()` 실패(비밀번호 불일치)

- `IllegalArgumentException`
    - 잘못된 요청값(중복값, 같은 권한 변경 시도 등)

- `PermissionDeniedException`
    - 관리자 권한 변경 정책 위반(권한 범위 밖 조작)

- `ForbiddenUsernameException`
    - 금지 username 정책 위반(compact 검사 포함)

---

### 5.4 컨트롤러 예외 처리 패턴(요약)

컨트롤러는 서비스 호출을 `try-catch`로 감싸고,
예외 유형별로 “폼 재표시 / redirect / 에러 페이지”를 결정한다.

- **회원가입(`/member/createProc`)**
    - `IllegalArgumentException`(중복 등) → 필드 에러 바인딩 후 `member/create`
    - 기타 예외 → `redirect:/error/runtimeErrorPage`

- **비밀번호 변경(`/member/passwordProc`)**
    - `WrongPasswordException` → `currentPassword` 필드 에러 후 `member/passwordUpdate`
    - `AccessDeniedException` / `EntityNotFoundException` → `redirect:/`
    - 기타 예외 → `redirect:/error/runtimeErrorPage`
    - 추가 차단(예외 아님): 로그인 방어 실패 → `redirect:/member/login`, `ROLE_BANNED` → `redirect:/`

- **회원 탈퇴(`/member/deleteProc`)**
    - `WrongPasswordException` → `password` 필드 에러 후 `member/delete`
    - `AccessDeniedException` / `EntityNotFoundException` → `redirect:/`
    - 기타 예외 → `redirect:/error/runtimeErrorPage`
    - 추가 차단(예외 아님): 로그인 방어 실패 → `redirect:/member/login`, `ROLE_BANNED` → `redirect:/`

- **권한 변경(`/member/roleProc`)**
    - `IllegalArgumentException` / `PermissionDeniedException`
        - 폼 재표시를 위해 `member`, `roles`를 model에 재주입
        - 글로벌 에러로 표시: `bindingResult.reject("roleChangeError", e.getMessage())`
        - `member/roleUpdate` 재표시
    - `AccessDeniedException` / `EntityNotFoundException` → `redirect:/`
    - 기타 예외 → `redirect:/error/runtimeErrorPage`

---

### 5.5 조회 화면 실패 처리(loadPathOrRedirect)

조회 화면은 “대상이 없으면 에러 대신 메인으로 복귀” UX를 가진다.

- `loadPathOrRedirect(int id, ...)`
    - `EntityNotFoundException` 발생 시 `redirect:/`

- `loadPathOrRedirect(Authentication, ...)`
    - `AccessDeniedException` 또는 `EntityNotFoundException` 발생 시 `redirect:/`

---

### 5.6 시스템 예외(예상치 못한 예외)

컨트롤러에서 명시적으로 처리하지 않은 예외는 마지막 `catch (Exception e)`에서 잡아:
- `redirect:/error/runtimeErrorPage`

---

## 6. 패키지 구조

프로젝트는 Member 도메인을 기준으로
**Controller → Service → Repository → Entity/DTO** 흐름을 갖는다.
공통 예외/검증 구성은 별도 패키지로 분리한다.

---

### 6.1 전체 구조 개요(예시)

- `member/` : 회원 도메인 핵심 코드(엔티티/DTO/리포지터리/서비스/컨트롤러/enum)
- `exception/` : 커스텀 비즈니스 예외 모음
- `validation/` : 검증 그룹(ValidationGroups), 권한 애노테이션(@ManagementOnly 등)

---

### 6.2 member 패키지

- **Entity**
    - `Member`
        - `@Entity`, `@Table(name="members")`
        - 주요 필드: `id`, `username(unique)`, `password(해시)`, `email(unique)`, `role(RoleType)`, `createDate`

- **DTO**
    - `MemberDTO` : 회원가입/권한변경 공용, 그룹 검증(OnCreate/OnUpdate), `normalize()`
    - `PasswordChangeDTO` : 비밀번호 변경 전용, confirm 일치 수동검증, `normalize()`
    - `MemberDeleteDTO` : 탈퇴 전용, confirm AssertTrue, reason(선택), `normalize()`

- **Repository**
    - `MemberRepository extends JpaRepository<Member, Integer>`
        - `findByUsername(String)`
        - `existsByUsername(String)`
        - `existsByEmail(String)`

- **Service**
    - `MemberService`
        - 조회: `list(Pageable)`, `view(id)`, `viewCurrentMember(Authentication)`
        - 회원가입: `setInsert(MemberDTO)`
        - 비밀번호 변경: `setUpdate(Authentication, PasswordChangeDTO)`
        - 탈퇴: `setDelete(Authentication, MemberDeleteDTO)`
        - 권한 변경(관리자): `updateRoleType(MemberDTO, Member admin)`

- **Controller**
    - `MemberController`
        - `/member/**` 라우팅, 모델 세팅(`data` 공통)
        - POST: 검증 + BindingResult 처리 + 서비스 호출
        - 접근 제어: `@PreAuthorize`, `@ManagementOnly`
        - 예외를 폼 재표시/redirect로 변환

- **Enum**
    - `RoleType` : `BANNED, USER, MANAGER, ADMIN`
    - `ReasonType` : `PRIVACY, CONTENT, SERVICE, ETC, NONE`

---

### 6.3 exception 패키지(커스텀 RuntimeException)

- `EntityNotFoundException` : 조회 실패(대상 없음)
- `ForbiddenUsernameException` : 금지 username 정책 위반
- `PermissionDeniedException` : 권한 변경 정책 위반
- `WrongPasswordException` : 비밀번호 불일치(matches 실패)

---

### 6.4 validation 패키지

- **ValidationGroups**
    - `OnCreate`, `OnUpdate`, `OnDelete`
    - 조합 그룹: `OnWrite extends OnCreate, OnUpdate`, `OnAll extends OnWrite, OnDelete`

- **권한 제어 애노테이션**
    - `@AdminOnly` : `@PreAuthorize("hasRole('ADMIN')")`
    - `@ManagementOnly` : `@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")`

---

### 6.5 레이어 의존 방향(요약)

- Controller → Service → Repository → DB
- DTO 검증은 Controller에서, DB 상태/권한/비밀번호 일치 같은 런타임 조건은 Service에서 검증
- 예외는 Service에서 발생시키고, Controller에서 UX(폼 재표시/redirect)로 변환
