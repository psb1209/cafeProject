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

    // DTO에서 id값을 꺼내오는 추상 메서드. 각 메서드에서 반드시 Override할 것.
    protected abstract Integer getIdFromDTO(D dto);

    // 전체 목록
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

    // 상세보기 (id로)
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

    // Optional을 반환하는 view 메서드
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

    // 등록
    @Transactional
    public D setInsert(D dto) {
        log.debug("[{} - insert] 로딩됨", entityClass.getSimpleName());
        beforeInsert(dto);
        log.debug("[{} - insert] beforeInsert 실행 성공", entityClass.getSimpleName());
        E entity = toEntity(dto);
        log.debug("[{} - insert] toEntity 실행 성공", entityClass.getSimpleName());
        afterInsert(dto, entity);
        log.debug("[{} - insert] afterInsert 실행 성공", entityClass.getSimpleName());
        return toDTO(repository.save(entity));
    }

    // 수정
    @Transactional
    public D setUpdate(D dto) {
        log.debug("[{} - update] 로딩됨", entityClass.getSimpleName());
        E entity = view(getIdFromDTO(dto));
        log.debug("[{} - update] entity 로딩 성공", entityClass.getSimpleName());
        beforeUpdate(dto, entity);
        log.debug("[{} - update] beforeUpdate 실행 성공", entityClass.getSimpleName());
        updateEntity(entity, dto);
        log.debug("[{} - update] updateEntity 실행 성공", entityClass.getSimpleName());
        afterUpdate(dto, entity);
        log.debug("[{} - update] afterUpdate 실행 성공", entityClass.getSimpleName());
        return toDTO(repository.save(entity));
    }

    // 삭제
    @Transactional
    public boolean setDelete(D dto) {
        log.debug("[{} - delete] 로딩됨", entityClass.getSimpleName());
        E entity = view(getIdFromDTO(dto));
        log.debug("[{} - delete] entity 로딩 성공", entityClass.getSimpleName());
        beforeDelete(entity);
        log.debug("[{} - delete] beforeDelete 실행 성공", entityClass.getSimpleName());
        repository.delete(entity);
        log.debug("[{} - delete] delete 성공", entityClass.getSimpleName());
        afterDelete(entity);
        log.debug("[{} - delete] afterDelete 실행 성공", entityClass.getSimpleName());
        return !repository.existsById(getIdFromDTO(dto));
    }

    // 이하의 메서드는 가급적이면 각 클래스에서 override해서 사용해주세요.
    // DTO → new Entity
    protected E toEntity(D dto) {
        return modelMapper.map(dto, entityClass);
    }
    // Entity → new DTO
    protected D toDTO(E entity) {
        return modelMapper.map(entity, dtoClass);
    }
    // 기존 Entity + DTO로 필드 갱신
    protected void updateEntity(E e, D d) {
        modelMapper.map(d, e);
    }

    // 이하의 메서드는 각 메서드의 기본 동작에 더해 추가 동작을 정의할 때 사용하는 메서드입니다.
    protected void beforeInsert(D dto) {}
    protected void afterInsert(D dto, E entity) {}
    protected void beforeUpdate(D dto, E entity) {}
    protected void afterUpdate(D dto, E entity) {}
    protected void beforeDelete(E entity) {}
    protected void afterDelete(E entity) {}
}
