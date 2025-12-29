# base 폴더에 대해
    Entity, DTO, Service, Controller의 기본 골격을 모아 둔 패키지입니다.
    extends를 통해 상속해서 사용할 수도 있고, 그대로 복사해서 수정하여 사용하셔도 됩니다.

## BaseEntity 사용 시 참고사항
    BaseEntity가 기본적으로 제공하는 필드는 다음과 같습니다.
    id         : 고유번호입니다. @Id + @GeneratedValue(IDENTITY)가 적용되어 있습니다.
    createDate : 생성일시입니다. @CreationTimestamp가 적용되어 있어 자동으로 값이 들어갑니다.
    member     : Member 엔티티입니다. 주로 작성자를 표현할 때 사용합니다.
    그 외의 필드(subject, content 등)는 각 엔티티에서 직접 정의해서 사용해 주세요.

## BaseDTO 사용 시 참고사항
    BaseDTO가 기본적으로 제공하는 필드는 다음과 같습니다.
    id         : 고유번호입니다.
    createDate : 생성일시입니다.
    memberId   : Member 엔티티의 고유번호입니다. Member 엔티티 그 자체가 아닙니다.
    username   : 화면 표시용 작성자 이름입니다. 일반적으로 Member.username을 복사해 담습니다.
    그 외의 필드(subject, content 등)는 각 DTO에서 직접 정의해서 사용해 주세요.
    ※ username은 입력/수정용이 아니라 출력용으로 사용하는 것을 권장합니다.
       작성자 변경 등은 memberId를 기준으로 처리해주세요.

    기본으로 제공되는 toEntity, toDTO, updateEntity는 단순 필드 이름 매핑만 처리합니다.
    Member와 같은 연관 엔티티를 DTO와 매핑하고 싶다면,
    각 서비스에서 toEntity, toDTO, updateEntity를 @Override하여 직접 변환 로직을 구현해 주세요.

    (자동 매핑을 위해 DTO에 바로 Member 엔티티 필드를 추가할 수도 있지만,
    DTO에 엔티티를 직접 넣는 방식은 권장하지 않으며,
    가급적 대신 식별자(memberId)와 화면 표시용 값(username)만 보관하는 것을 추천드립니다.)

## BaseCrudService 사용 시 참고사항
    BaseCrudService는 제네릭 타입을 사용합니다.
    따라서 상속받을 때 해당 타입에 대한 정보를 넘겨주어야 합니다.
    - E : Entity 타입
    - D : DTO 타입

    제네릭 타입을 지정하는 방식은 Repository를 정의할 때와 같습니다.
        예) extends BaseCrudService<Example, ExampleDTO>

    BaseCrudService는 다음 네 가지 정보를 필요로 합니다.
    * JpaRepository<E, Integer> repository
    * ModelMapper modelMapper
    * Class<E> entityClass
    * Class<D> dtoClass

    따라서 자식 서비스에서 아래와 같이 super(...) 를 호출해 주어야 합니다.
    
    @Service
    public class ExampleService extends BaseCrudService<Example, ExampleDTO> {
        public ExampleService(
                ExampleRepository exampleRepository,
                ModelMapper modelMapper
        ) {
            super(exampleRepository, modelMapper, Example.class, ExampleDTO.class);
        }
    
        @Override
        protected Integer getIdFromDTO(ExampleDTO dto) { return dto.getId(); }
    }

    이때, 서비스에서 update 등은 DTO의 id를 사용하므로,
    각 메서드에서 반드시 getIdFromDto(D dto) 메서드를 @Override해주어야 합니다.

    또한, BaseCrudService는 toEntity(D dto), toDTO(E entity), updateEntity(E entity, D dto)를
    ModelMapper를 사용한 메서드로 기본 제공하므로,
    가급적이면 각 서비스에서 @Override해서 DTO ↔ Entity 변환 로직을 구현해 주세요.

### CRUD 메서드 요약
#### 목록 조회
    public Page<E> list(Pageable pageable)
    public Page<D> listDTO(Pageable pageable)
    
    * list(...) : Entity 그대로 Page<E> 반환
    * listDTO(...) : 내부적으로 list(...) 호출 후 toDTO로 변환하여 Page<D> 반환

#### 상세 조회
    public E view(int id)
    public D viewDTO(int id)
    public Optional<E> viewOptional(int id)
    public Optional<D> viewOptionalDTO(int id)
    
    * view(id) : 존재하지 않으면 EntityNotFoundException 발생, Entity 반환
    * viewDTO(id) : view(id) 결과를 toDTO로 변환하여 D 반환
    * viewOptional(id) : Optional<E> 로 반환 (없으면 Optional.empty())
    * viewOptionalDTO(id) : Optional<E>를 toDTO로 변환하여 Optional<D> 반환

#### 추가
    @Transactional
    public void setInsert(D dto)
    
    1. beforeInsert(dto)
    2. toEntity(dto) → E 엔티티 생성
    3. repository.save(entity) 저장
    4. afterInsert(dto, entity)

#### 수정
    @Transactional
    public void setUpdate(D dto)
    
    1. view(getIdFromDTO(dto)) 로 기존 엔티티 조회
    2. beforeUpdate(dto, entity)
    3. updateEntity(entity, dto) 로 필드 갱신
    4. repository.save(entity) 저장
    5. afterUpdate(dto, entity)

#### 삭제
    @Transactional
    public void setDelete(D dto)
    
    1. view(getIdFromDTO(dto)) 로 기존 엔티티 조회
    2. beforeDelete(entity)
    3. repository.delete(entity) 삭제
    4. afterDelete(entity)

### 훅(Hook) 메서드로 추가 동작 정의하기
    BaseCrudService는 추가/수정/삭제 시점에 추가 동작을 끼워 넣을 수 있도록
    다음과 같은 훅 메서드를 제공합니다.
        protected void beforeInsert(D dto) {}
        protected void afterInsert(D dto, E entity) {}
        protected void beforeUpdate(D dto, E entity) {}
        protected void afterUpdate(D dto, E entity) {}
        protected void beforeDelete(E entity) {}
        protected void afterDelete(E entity) {}

    각 메서드의 의미는 다음과 같습니다.
    - beforeInsert : Entity 생성 전에 DTO를 검증하거나 기본값을 채울 때 사용합니다.
        (예: 현재 로그인한 회원 ID 설정, 생성일시 기본값 설정 등)
    - afterInsert : Entity가 생성된 이후에 후처리가 필요할 때 사용합니다.
        (예: 로그 기록, 연관 데이터 생성 등)
    - beforeUpdate : 수정 전에 기존 Entity와 DTO를 함께 비교/검증할 때 사용합니다.
        (예: 작성자 변경 금지, 특정 상태에서는 수정 불가 등)
    - afterUpdate : 수정 후 후처리가 필요할 때 사용합니다.
        (예: 로그 기록, 캐시 갱신, 통계 업데이트 등)
    - beforeDelete : 삭제 전 검증이 필요할 때 사용합니다.
        (예: 권한 체크, 연관 데이터 검증 등)
    - afterDelete : 삭제 후 추가 정리가 필요할 때 사용합니다.
        (예: 실제 파일 삭제, 통계/집계 데이터 갱신, 삭제 로그 기록 등)

    필요하지 않은 서비스에서는 @Override하지 않아도 되며,
    특정 Entity에서 추가 로직이 필요한 경우에만 선택적으로 @Override해서 사용하면 됩니다.

## BaseCrudController 사용 시 참고사항
    BaseCrudController는 제네릭 타입을 사용합니다.
    따라서 상속받을 때 해당 타입에 대한 정보를 넘겨주어야 합니다.
    - E : Entity 타입
    - D : DTO 타입

    제네릭 타입을 지정하는 방식은 Repository를 정의할 때와 같습니다.
        예) extends BaseCrudController<Example, ExampleDTO>

    BaseCrudController는 Service와 기본 주소(basePath)를 필요로 하므로,
    자식 컨트롤러에서 super(service, basePath)를 호출해야 합니다.
    다음과 같은 생성자를 추가해서 사용해 주세요.
        public ExampleController(ExampleService exampleService) {
            super(exampleService, "example"); // "example"은 뷰 폴더 이름과 맞춰 주세요.
        }

    만약 BaseCrudController의 메서드(view, list 등)를 @Override해서
    개별적으로 동작을 바꾸고 싶다면, 생성자에서 서비스 필드를 따로 보관해도 됩니다.
        private final ExampleService exampleService;
        public ExampleController(ExampleService exampleService) {
            super(exampleService, "example");
            this.exampleService = exampleService;
        }

    BaseCrudController에서 getNotFoundRedirectPath()를 @Override하면,
    view 결과가 없을 때 redirect될 링크를 원하는 대로 지정할 수 있습니다.
    기본값은 "redirect:/" 입니다.

### BaseCrudController에서 제공하는 링크
    기본 제공되는 URL은 다음과 같습니다. (예: /example 기준)
    GET  /example/list           : 리스트
    GET  /example/view/{id}      : 상세보기
    GET  /example/create         : 추가
    GET  /example/update/{id}    : 수정
    GET  /example/delete/{id}    : 삭제
    POST /example/createProc     : 추가 처리
    POST /example/updateProc     : 수정 처리
    POST /example/deleteProc     : 삭제 처리

    컨트롤러 메서드를 @Override 해서 검증(@Valid, BindingResult)이나
    권한 체크(@PreAuthorize) 등을 추가로 넣을 수 있습니다.

## BaseImageService, BaseImageController
    BaseCrudService, BaseCrudController에 이미지 업로드 관련 기능을 추가한 클래스입니다.
    application.properties에서 다음 설정을 확인해주세요.

    실제 파일이 저장될 폴더 (물리 경로)
    app.image.upload-dir=C:/dw202/attach/summernote/
    클라이언트가 img src로 쓸 URL prefix
    app.image.url-prefix=/dw202/attach/summernote/

    가능하면 마지막 경로 끝에 '/'를 붙이는 것을 권장합니다.

    BaseImageService를 사용할 때, 삭제한 게시글의 이미지를 실제 파일에서도 삭제하고 싶다면
    afterDelete 메서드를 @Override해서 다음과 같은 동작을 추가해주세요.
        @Override
        protected void afterDelete(E entity) {
            List<String> imageUrls = extractImageUrls(entity.getContent());
            deleteImageFiles(imageUrls);
        }

    BaseImageController에서 기본적으로 제공하는 이미지 업로드 url은 다음과 같습니다.
    POST /example/uploadImage    : 이미지 업로드 (/example 기준)

## BaseUtility
    문자열/시간/수학(분수) 관련 공용 유틸을 모아 둔 클래스입니다.

### 1) 한글 정규화(검색용 Key 생성)
    게시글/게시판 제목처럼 "한글 + 공백 + 영문/숫자"가 섞인 문자열을 검색 친화적인 Key로 바꿔줍니다.

    제공 메서드
    - toKey(s) : 한글 음절을 초/중/종성으로 분해해 이어붙임
        예) "사과" → "ㅅㅏㄱㅘ", "삭제" → "ㅅㅏㄱㅈㅔ"
    - toChosungKey(s) : 한글 음절에서 초성만 추출
        예) "사과" → "ㅅㄱ", "삭제" → "ㅅㅈ"
    - jaeumCutter(ch) : 겹자음(ㄳ, ㄵ …)을 분해
    - jaeumBreaker(s) : 문자열의 겹자음을 전부 분해한 뒤 초성 19개만 남김
    - isChosungQuery(s) : 사용자가 "ㅅㄱ"처럼 초성만 입력했는지 판별

    사용 예시
    - DB에 titleChosungKey = BaseUtility.toChosungKey(title) 형태로 저장해두고,
      사용자가 입력한 keyword에 대해 isChosungQuery(keyword)면 초성 컬럼으로, 아니면 일반 컬럼으로 검색 분기할 때 사용합니다.

### 2) 시간 포맷 유틸(Timestamp → String)
    Timestamp를 지정한 타임존/패턴으로 문자열로 바꿉니다.
    - 기본 패턴: "yyyy-MM-dd HH:mm:ss"
    - 기본 타임존: "Asia/Seoul"

    제공 메서드
    - formatTimestamp(ts) : 기본 패턴/타임존으로 포맷
    - formatTimestamp(ts, pattern) : 패턴을 지정해서 포맷
    - formatTimestamp(ts, pattern, zoneId) : 패턴과 타임존 모두 지정해서 포맷

### 3) 분수(유리수) 계산 유틸: BaseUtility.Rational
    주사위/확률 계산처럼 유리수 연산을 정확한 분수 형태로 다루고 싶을 때 쓰는 간단한 유틸 클래스입니다.
    - 생성 시 약분(gcd) 및 분모 부호 정규화
    - 주의: long 기반이기 때문에 큰 값/연속 연산에는 적합하지 않습니다.

    생성자
    - BaseUtility.Rational(long numerator, long denominator)
     - numerator   : 분자
     - denominator : 분모

    제공 메서드
    - add      : 덧셈
    - subtract : 뺄셈
    - multiply : 곱셈
    - divide   : 나눗셈
    - negate   : 부호 반전
    - toDouble : Double으로 변환

## HTML에서 데이터 불러올 때 참고사항
    BaseCrudController를 상속받을 경우 개별 데이터는 "data",
    목록 데이터는 "list"라는 이름으로 Model에 담겨 전달됩니다.

    예시 (Thymeleaf):

        <!-- 목록 -->
        <tr th:each="item : ${list}">
            <td th:text="${item.id}"></td>
            <td th:text="${item.subject}"></td>
        </tr>

        <!-- 상세 -->
        <h1 th:text="${data.subject}"></h1>
        <div th:text="${data.content}"></div>

    별도의 이름으로 데이터를 넘기고 싶으신 경우,
    BaseCrudController를 상속하지 않고 직접 컨트롤러를 구현하시거나
    해당 메서드만 Override하여 Model에 원하는 이름으로 데이터를 추가해 주세요.
