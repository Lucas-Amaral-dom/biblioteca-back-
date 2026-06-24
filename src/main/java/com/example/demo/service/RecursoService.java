package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.dto.RecursoDTO;
import com.example.demo.entity.Recurso;
import com.example.demo.repository.RecursoRepository;

@Service
public class RecursoService extends BaseService<Recurso, RecursoDTO> {

    public RecursoService(RecursoRepository repository){
        super(repository);
    }
    @Override
    public RecursoDTO toDto(Recurso entity) {
        RecursoDTO dto = new RecursoDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setDescricao(entity.getDescricao());
        dto.setTipo(entity.getTipo());
        dto.setCapacidade(entity.getCapacidade());
        dto.setStatus(entity.getStatus());
        dto.setCodigo(entity.getCodigo());
        dto.setLaboratorio(entity.getLaboratorio());
        dto.setMesa(entity.getMesa());
        return dto;
    }

    @Override
    public Recurso toEntity(RecursoDTO dto) {
        Recurso entity = new Recurso();
        entity.setId(dto.getId());
        entity.setNome(dto.getNome());
        entity.setDescricao(dto.getDescricao());
        entity.setTipo(dto.getTipo());
        entity.setCapacidade(dto.getCapacidade());
        entity.setStatus(dto.getStatus());
        entity.setCodigo(dto.getCodigo());
        entity.setLaboratorio(dto.getLaboratorio());
        entity.setMesa(dto.getMesa());
        return entity;
    }

}
