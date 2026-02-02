package com.Fabricio.agendadortarefas.infrastructure.entity;

import com.Fabricio.agendadortarefas.infrastructure.enums.StatusNotificacaoEnum;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("tarefas")
public class TarefasEntity {

    @Id
    private String id;
    private String nomeTarefa;
    private String descricao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataEvento;
    private String emailUsuario;
    private LocalDateTime dataAlteracao;
    private StatusNotificacaoEnum statusNotificaoEnum;
}
