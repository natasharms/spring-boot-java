package io.github.natasharms.domain.service.impl;

import io.github.natasharms.domain.entity.Cliente;
import io.github.natasharms.domain.entity.ItemPedido;
import io.github.natasharms.domain.entity.Pedido;
import io.github.natasharms.domain.entity.Produto;
import io.github.natasharms.domain.enums.StatusPedido;
import io.github.natasharms.domain.exception.RegraNegocioException;
import io.github.natasharms.domain.repository.Clientes;
import io.github.natasharms.domain.repository.ItemsPedido;
import io.github.natasharms.domain.repository.Pedidos;
import io.github.natasharms.domain.repository.Produtos;
import io.github.natasharms.domain.service.PedidoService;
import io.github.natasharms.exception.PedidoNaoEncontradoException;
import io.github.natasharms.rest.dto.ItemPedidoDTO;
import io.github.natasharms.rest.dto.PedidoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final Pedidos repository;
    private final Clientes clientesRepository;
    private final Produtos produtosRepository;
    private final ItemsPedido itemsPedidoRepository;


    @Override
    @Transactional
    public Pedido salvar(PedidoDTO dto) {
        Integer idCliente = dto.getCliente();
        Cliente cliente = clientesRepository.findById(idCliente)
                .orElseThrow(() -> new RegraNegocioException("Codigo cliente invalido"));

        Pedido pedido = new Pedido();
        pedido.setTotal(dto.getTotal());
        pedido.setDataPedido(LocalDate.now());
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.REALIZADO);

        List<ItemPedido>  itemsPedido = converterItems(pedido, dto.getItems());
        repository.save(pedido);
        itemsPedidoRepository.saveAll(itemsPedido);
        pedido.setItens(itemsPedido);

        return pedido;
    }

    @Override
    public Optional<Pedido> obterPedidoCompleto(Integer id) {
        return repository.findByIdFetchItens(id);
    }

    @Override
    @Transactional
    public void atualizaStatus(Integer id, StatusPedido statusPedido) {
        repository.findById(id)
                .map(pedido -> {
                    pedido.setStatus(statusPedido);
                    return repository.save(pedido);
                }).orElseThrow (() -> new PedidoNaoEncontradoException());

    }

    private List<ItemPedido> converterItems (Pedido pedido, List<ItemPedidoDTO> items) {
        if (items.isEmpty()) {
            throw new RegraNegocioException ("Nao é possivel fazer um pedido sem itens");
        }

        return items
                .stream()
                .map(dto -> {
                    Integer idProduto = dto.getProduto();
                    Produto produto = produtosRepository
                            .findById(idProduto)
                            .orElseThrow(() -> new RegraNegocioException("Produto Invalido: " + idProduto));
                    ItemPedido itemPedido = new ItemPedido();
                    itemPedido.setQuantidade(dto.getQuantidade());
                    itemPedido.setPedido(pedido);
                    itemPedido.setProduto(produto);
                    return itemPedido;
                }).collect(Collectors.toList());
    }
}