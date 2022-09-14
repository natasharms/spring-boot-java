package io.github.natasharms.domain.service;

import io.github.natasharms.domain.entity.Pedido;
import io.github.natasharms.domain.enums.StatusPedido;
import io.github.natasharms.rest.dto.PedidoDTO;

import java.util.Optional;

public interface PedidoService {
    Pedido salvar (PedidoDTO dto);

    Optional <Pedido> obterPedidoCompleto(Integer id);

    void atualizaStatus(Integer id, StatusPedido statusPedido);
}
