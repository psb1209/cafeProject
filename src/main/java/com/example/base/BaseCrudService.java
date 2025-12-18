package com.example.base;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.exception.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
public abstract class BaseCrudService<E, D> {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final JpaRepository<E, Integer> repository;
    protected final ModelMapper modelMapper;
    private final Class<E> entityClass;
    private final Class<D> dtoClass;

    protected BaseCrudService(
            JpaRepository<E, Integer> repository,
            ModelMapper modelMapper,
            Class<E> entityClass,
            Class<D> dtoClass
    ) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.entityClass = entityClass;
        this.dtoClass = dtoClass;
    }

    /**
     * DTO에서 id값을 꺼내오는 추상 메서드.
     * BaseCrudService는 DTO 구조를 모르므로 각 서비스에서 id 추출 방식을 확정해야 합니다.
     * 각 서비스에서 반드시 @Override해주세요.
     */
    public abstract Integer getIdFromDTO(D dto);

    /**
     * 전체 목록 조회 (페이징 포함).
     * - page, size, sort 조건을 로그로 남기고
     * - 결과 Page 정보(전체 개수, 전체 페이지, 현재 페이지 요소 수)도 같이 로그로 남깁니다.
     */
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
    public D viewDTO(int id) throws EntityNotFoundException {
        D dto = toDTO(view(id));
        log.debug("viewDTO - [{}] 변환됨", dtoClass.getSimpleName());
        return dto;
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
    public Optional<D> viewOptionalDTO(int id) {
        Optional<D> result = viewOptional(id).map(this::toDTO);
        log.debug("viewOptionalDTO - [{}] 변환됨", dtoClass.getSimpleName());
        return result;
    }

    /** 등록 처리 */
    @Transactional
    public void setInsert(D dto) {
        log.debug("[{} - setInsert] 호출됨", entityClass.getSimpleName());
        beforeInsert(dto);
        log.debug("[{} - setInsert] beforeInsert 실행 성공", entityClass.getSimpleName());
        E entity = toEntity(dto);
        log.debug("[{} - setInsert] toEntity 실행 성공", entityClass.getSimpleName());
        repository.save(entity);
        log.debug("[{} - setInsert] save 성공", entityClass.getSimpleName());
        afterInsert(dto, entity);
        log.debug("[{} - setInsert] afterInsert 실행 성공", entityClass.getSimpleName());
    }

    /** 수정 처리 */
    @Transactional
    public void setUpdate(D dto) {
        log.debug("[{} - setUpdate] 호출됨", entityClass.getSimpleName());
        E entity = view(getIdFromDTO(dto));
        log.debug("[{} - setUpdate] entity 로딩 성공", entityClass.getSimpleName());
        beforeUpdate(dto, entity);
        log.debug("[{} - setUpdate] beforeUpdate 실행 성공", entityClass.getSimpleName());
        updateEntity(entity, dto);
        log.debug("[{} - setUpdate] updateEntity 실행 성공", entityClass.getSimpleName());
        repository.save(entity);
        log.debug("[{} - setUpdate] save 성공", entityClass.getSimpleName());
        afterUpdate(dto, entity);
        log.debug("[{} - setUpdate] afterUpdate 실행 성공", entityClass.getSimpleName());
    }

    /** 삭제 처리 */
    @Transactional
    public void setDelete(D dto) {
        log.debug("[{} - setDelete] 호출됨", entityClass.getSimpleName());
        E entity = view(getIdFromDTO(dto));
        log.debug("[{} - setDelete] entity 로딩 성공", entityClass.getSimpleName());
        beforeDelete(entity);
        log.debug("[{} - setDelete] beforeDelete 실행 성공", entityClass.getSimpleName());
        repository.delete(entity);
        log.debug("[{} - setDelete] delete 성공", entityClass.getSimpleName());
        afterDelete(entity);
        log.debug("[{} - setDelete] afterDelete 실행 성공", entityClass.getSimpleName());
    }

    /** 공백 상태인 새 Entity 만들기 */
    public E newEntity() {
        try {
            return entityClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Entity 생성 실패 : " + entityClass.getName(), e);
        }
    }
    /** 공백 상태인 새 DTO 만들기 */
    public D newDTO() {
        try {
            return dtoClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("DTO 생성 실패 : " + dtoClass.getName(), e);
        }
    }

    // 이하의 메서드는 가급적이면 각 클래스에서 @override해서 사용해주세요.
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

    // 이하의 메서드는 각 메서드의 기본 동작에 더해 추가 동작을 정의할 때 사용하는 메서드입니다.

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
}
