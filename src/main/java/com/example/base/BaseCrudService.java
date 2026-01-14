package com.example.base;

import com.example.exception.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Transactional(readOnly = true)
public abstract class BaseCrudService<E, D> {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final JpaRepository<E, Integer> repository;
    protected final ModelMapper modelMapper;

    private final Class<E> entityClass;
    private final Class<D> dtoClass;

    // 상속 없이도 id 추출 가능하도록 전략화
    private final Function<D, Integer> dtoIdGetter;

    // no-args 생성자 없는 DTO/Entity도 커버
    private final Supplier<E> entityFactory;
    private final Supplier<D> dtoFactory;

    protected BaseCrudService(
            JpaRepository<E, Integer> repository,
            ModelMapper modelMapper,
            Class<E> entityClass,
            Class<D> dtoClass
    ) {
        this(repository, modelMapper, entityClass, dtoClass, null, null, null);
    }

    /**
     * @param dtoIdGetter   DTO에서 id를 뽑는 함수(선택). 없으면 HasId/getId/id 필드 순으로 자동 탐색
     * @param entityFactory Entity 생성 팩토리(선택). 없으면 reflection(no-args) 사용
     * @param dtoFactory    DTO 생성 팩토리(선택). 없으면 reflection(no-args) 사용
     */
    protected BaseCrudService(
            JpaRepository<E, Integer> repository,
            ModelMapper modelMapper,
            Class<E> entityClass,
            Class<D> dtoClass,
            Function<D, Integer> dtoIdGetter,
            Supplier<E> entityFactory,
            Supplier<D> dtoFactory
    ) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;

        this.dtoIdGetter = (dtoIdGetter != null) ? dtoIdGetter : this::defaultGetIdFromDTO;
        this.entityFactory = entityFactory;
        this.dtoFactory = dtoFactory;

        // updateEntity에서 null 덮어쓰기 사고 방지(기본값)
        // "null로 지우기"가 필요하면 개별 서비스에서 설정을 바꿔도 됨
        this.modelMapper.getConfiguration().setSkipNullEnabled(true);
    }

    /**
     * DTO에서 id값을 꺼내옵니다.
     * - 기본은 HasId/getId()/id필드 순으로 자동 탐색
     * - DTO 구조가 특이하면 생성자에 dtoIdGetter 주입 or override 권장
     */
    public Integer getIdFromDTO(D dto) {
        return dtoIdGetter.apply(dto);
    }

    protected Integer defaultGetIdFromDTO(D dto) {
        if (dto == null) return null;

        // 1) HasId 우선
        if (dto instanceof HasId<?> hasId) {
            Object id = hasId.getId();
            if (id == null) return null;
            if (id instanceof Integer i) return i;
            throw new IllegalStateException("HasId.getId()가 Integer가 아닙니다. type=" + id.getClass().getName());
        }

        // 2) public getId() 메서드 탐색
        try {
            Method m = dto.getClass().getMethod("getId");
            Object id = m.invoke(dto);
            if (id == null) return null;
            if (id instanceof Integer i) return i;
        } catch (NoSuchMethodException ignored) {
            // pass
        } catch (Exception e) {
            throw new IllegalStateException("DTO getId() 호출 실패: " + dto.getClass().getName(), e);
        }

        // 3) id 필드 탐색
        try {
            Field f = dto.getClass().getDeclaredField("id");
            f.setAccessible(true);
            Object id = f.get(dto);
            if (id == null) return null;
            if (id instanceof Integer i) return i;
        } catch (NoSuchFieldException ignored) {
            // pass
        } catch (Exception e) {
            throw new IllegalStateException("DTO id 필드 접근 실패: " + dto.getClass().getName(), e);
        }

        throw new IllegalStateException(
                "DTO에서 id를 추출할 수 없습니다: " + dto.getClass().getName() + "\n" +
                        "해결 방법:\n" +
                        "1) DTO가 HasId<Integer>를 구현하거나\n" +
                        "2) public Integer getId()를 제공하거나\n" +
                        "3) id 필드를 두거나\n" +
                        "4) 서비스 생성자에서 dtoIdGetter를 넘기거나\n" +
                        "5) getIdFromDTO를 override 하세요."
        );
    }

    /* =========================
       조회
       ========================= */

    public Page<E> list(Pageable pageable) {
        log.debug("[{}] 목록 조회 요청, page={}, size={}, sort={}",
                entityClass.getSimpleName(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        Page<E> page = repository.findAll(pageable);

        log.debug("[{}] 목록 조회 결과, totalElements={}, totalPages={}, currentElements={}",
                entityClass.getSimpleName(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumberOfElements());

        return page;
    }

    public Page<D> listDTO(Pageable pageable) {
        Page<D> page = list(pageable).map(this::toDTO);
        log.debug("[listDTO] - [{}] 변환됨", dtoClass.getSimpleName());
        return page;
    }

    /**
     * 상세 보기 메서드 (id 기준)
     * - 존재하지 않으면 EntityNotFoundException이 발생합니다.
     */
    public E view(int id) throws EntityNotFoundException {
        log.debug("[{}] Entity 조회 시도, id={}", entityClass.getSimpleName(), id);
        E entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 id=" + id));
        log.debug("[{}] Entity 조회 성공, id={}", entityClass.getSimpleName(), id);
        return entity;
    }
    public E view(Integer id) throws EntityNotFoundException {
        requireId(id);
        return view(id.intValue());
    }

    public D viewDTO(int id) throws EntityNotFoundException {
        D dto = toDTO(view(id));
        log.debug("viewDTO - [{}] 변환됨", dtoClass.getSimpleName());
        return dto;
    }
    public D viewDTO(Integer id) throws EntityNotFoundException {
        requireId(id);
        return viewDTO(id.intValue());
    }

    /**
     * Optional 버전 view 메서드 (id 기준)
     * - 예외를 던지지 않고 Optional로 감싸서 반환합니다.
     * - 호출 측에서 isPresent()/orElse() 등으로 유연하게 처리하고 싶을 때 사용합니다.
     */
    public Optional<E> viewOptional(int id) {
        log.debug("[{}] Optional 조회 시도, id={}", entityClass.getSimpleName(), id);
        Optional<E> result = repository.findById(id);
        log.debug("[{}] Optional 조회 결과, id={}, present={}",
                entityClass.getSimpleName(), id, result.isPresent());
        return result;
    }

    public Optional<E> viewOptional(Integer id) {
        if (id == null) return Optional.empty();
        return viewOptional(id.intValue());
    }

    public Optional<D> viewOptionalDTO(int id) {
        Optional<D> result = viewOptional(id).map(this::toDTO);
        log.debug("viewOptionalDTO - [{}] 변환됨", dtoClass.getSimpleName());
        return result;
    }

    public Optional<D> viewOptionalDTO(Integer id) {
        return viewOptional(id).map(this::toDTO);
    }

    /* =========================
       쓰기 (하위호환: setInsert/setUpdate/setDelete 유지)
       ========================= */

    /** 등록 처리 */
    @Transactional
    public E insert(D dto) {
        log.debug("[{} - insert] 호출됨", entityClass.getSimpleName());
        beforeInsert(dto);
        log.debug("[{} - insert] beforeInsert 실행 성공", entityClass.getSimpleName());

        E entity = toEntity(dto);
        log.debug("[{} - insert] toEntity 실행 성공", entityClass.getSimpleName());

        E saved = repository.save(entity);
        log.debug("[{} - insert] save 성공", entityClass.getSimpleName());

        afterInsert(dto, saved);
        log.debug("[{} - insert] afterInsert 실행 성공", entityClass.getSimpleName());

        return saved;
    }

    /** 수정 처리 */
    @Transactional
    public E update(D dto) {
        log.debug("[{} - update] 호출됨", entityClass.getSimpleName());

        Integer id = getIdFromDTO(dto);
        requireId(id);

        E entity = view(id);
        log.debug("[{} - update] entity 로딩 성공", entityClass.getSimpleName());

        beforeUpdate(dto, entity);
        log.debug("[{} - update] beforeUpdate 실행 성공", entityClass.getSimpleName());

        updateEntity(entity, dto);
        log.debug("[{} - update] updateEntity 실행 성공", entityClass.getSimpleName());

        E saved = repository.save(entity);
        log.debug("[{} - update] save 성공", entityClass.getSimpleName());

        afterUpdate(dto, saved);
        log.debug("[{} - update] afterUpdate 실행 성공", entityClass.getSimpleName());

        return saved;
    }

    /** 삭제 처리 */
    @Transactional
    public void delete(D dto) {
        log.debug("[{} - delete] 호출됨", entityClass.getSimpleName());

        Integer id = getIdFromDTO(dto);
        requireId(id);

        E entity = view(id);
        log.debug("[{} - delete] entity 로딩 성공", entityClass.getSimpleName());

        beforeDelete(entity);
        log.debug("[{} - delete] beforeDelete 실행 성공", entityClass.getSimpleName());

        repository.delete(entity);
        log.debug("[{} - delete] delete 성공", entityClass.getSimpleName());

        afterDelete(entity);
        log.debug("[{} - delete] afterDelete 실행 성공", entityClass.getSimpleName());
    }

    @Transactional
    public void setInsert(D dto) {
        insert(dto);
    }

    @Transactional
    public void setUpdate(D dto) {
        update(dto);
    }

    @Transactional
    public void setDelete(D dto) {
        delete(dto);
    }

    /* =========================
       팩토리
       ========================= */

    public E newEntity() {
        if (entityFactory != null) return entityFactory.get();
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Entity 생성 실패 : " + entityClass.getName(), e);
        }
    }

    public D newDTO() {
        if (dtoFactory != null) return dtoFactory.get();
        try {
            return dtoClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("DTO 생성 실패 : " + dtoClass.getName(), e);
        }
    }

    // 이하의 메서드는 가급적이면 각 클래스에서 @Override 해서 사용해주세요.
    /** DTO → new Entity */
    protected E toEntity(D dto) {
        return modelMapper.map(dto, entityClass);
    }

    /** Entity → new DTO */
    protected D toDTO(E entity) {
        return modelMapper.map(entity, dtoClass);
    }

    /** 기존 Entity + DTO로 필드 갱신 */
    protected void updateEntity(E e, D d) {
        modelMapper.map(d, e);
    }

    /* =========================
       Hooks
       ========================= */

    /**
     * Entity 생성 전에 DTO를 검증하거나 기본값을 채울 때 사용합니다.
     * (예: 현재 로그인한 회원 ID 설정, 생성일시 기본값 설정 등)
     */
    protected void beforeInsert(D dto) {}

    /**
     * Entity가 생성된 이후에 후처리가 필요할 때 사용합니다.
     * (예: 로그 기록, 연관 데이터 생성 등)
     */
    protected void afterInsert(D dto, E entity) {}

    /**
     * 수정 전에 기존 Entity와 DTO를 함께 비교/검증할 때 사용합니다.
     * (예: 작성자 변경 금지, 특정 상태에서는 수정 불가 등)
     */
    protected void beforeUpdate(D dto, E entity) {}

    /**
     * 수정 후 후처리가 필요할 때 사용합니다.
     * (예: 로그 기록, 캐시 갱신, 통계 업데이트 등)
     */
    protected void afterUpdate(D dto, E entity) {}

    /**
     * 삭제 전 검증이 필요할 때 사용합니다.
     * (예: 권한 체크, 연관 데이터 검증 등)
     */
    protected void beforeDelete(E entity) {}

    /**
     * 삭제 후 추가 정리가 필요할 때 사용합니다.
     * (예: 실제 파일 삭제, 통계/집계 데이터 갱신, 삭제 로그 기록 등)
     */
    protected void afterDelete(E entity) {}

    /* ========================= */

    protected void requireId(Integer id) {
        if (id == null) throw new IllegalArgumentException("id가 없습니다.");
    }
}
