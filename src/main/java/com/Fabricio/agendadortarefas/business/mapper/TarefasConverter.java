package com.Fabricio.agendadortarefas.business.mapper;


import com.Fabricio.agendadortarefas.business.dto.TarefasDTO;
import com.Fabricio.agendadortarefas.infrastructure.entity.TarefasEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TarefasConverter {

    TarefasEntity paraTarefaEntity(TarefasDTO dto);
    TarefasDTO paraTarefaDTO(TarefasEntity entity);


}
