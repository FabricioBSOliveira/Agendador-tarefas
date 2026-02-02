package com.Fabricio.agendadortarefas.business;

import com.Fabricio.agendadortarefas.business.dto.TarefasDTO;
import com.Fabricio.agendadortarefas.business.mapper.TarefasConverter;
import com.Fabricio.agendadortarefas.infrastructure.entity.TarefasEntity;
import com.Fabricio.agendadortarefas.infrastructure.enums.StatusNotificacaoEnum;
import com.Fabricio.agendadortarefas.infrastructure.repository.TarefasRepository;
import com.Fabricio.agendadortarefas.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TarefasService {

    private final TarefasRepository tarefasRepository;
    private final TarefasConverter tarefaConverter;
    private final JwtUtil jwtUtil;

    public TarefasDTO gravarTarefa(String token, TarefasDTO dto){
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        dto.setEmailUsuario(email);
        dto.setDataCriacao(LocalDateTime.now());
        dto.setStatusNotificaoEnum(StatusNotificacaoEnum.PENDENTE);
        TarefasEntity entity = tarefaConverter.paraTarefaEntity(dto);
        return tarefaConverter.paraTarefaDTO(
                tarefasRepository.save(entity));
    }

}
