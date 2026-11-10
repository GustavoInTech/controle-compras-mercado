package com.gustavointech.compras.nota;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gustavointech.compras.leitor.ItemLido;
import com.gustavointech.compras.leitor.LeitorNfce;
import com.gustavointech.compras.leitor.MercadoLido;
import com.gustavointech.compras.leitor.NotaLida;
import com.gustavointech.compras.mercado.Mercado;
import com.gustavointech.compras.mercado.MercadoRepository;
import com.gustavointech.compras.usuario.UsuarioRepository;

@Service
public class SalvarNotaService {

    private static final int MAX_NOME_ITEM = 300;
    private static final int MAX_CODIGO_ITEM = 40;
    private static final int MAX_UNIDADE = 10;
    private static final BigDecimal LIMITE_DINHEIRO = new BigDecimal("10000000");
    private static final BigDecimal LIMITE_QUANTIDADE = new BigDecimal("1000000");

    // O leitor é Java puro e não guarda estado: pode ser criado aqui mesmo
    private final LeitorNfce leitor = new LeitorNfce();
    private final NotaRepository notas;
    private final MercadoRepository mercados;
    private final UsuarioRepository usuarios;

    public SalvarNotaService(NotaRepository notas, MercadoRepository mercados, UsuarioRepository usuarios) {
        this.notas = notas;
        this.mercados = mercados;
        this.usuarios = usuarios;
    }

    // Lê o texto DE NOVO (não confia no que o navegador devolveria), confere tudo e
    // grava numa transação só:
    // ou grava o mercado (se for novo), a nota e todos os itens, ou não grava nada.
    @Transactional
    public NotaSalvaResponse salvar(Long usuarioId, String texto, String link, LocalDate dataInformada) {
        NotaLida lida = leitor.ler(texto);

        String chave = IdentificadorNota.chave(lida, link);
        // A data do texto vale mais que a informada pelo usuário
        LocalDate data = lida.getDataEmissao() != null ? lida.getDataEmissao() : dataInformada;

        List<String> motivos = conferir(lida, chave, data);
        if (!motivos.isEmpty()) {
            throw new NotaInvalidaException(motivos);
        }

        String impressao = IdentificadorNota.impressaoDigital(lida, data);
        String identificador = IdentificadorNota.de(lida, chave);
        if (identificador == null) {
            identificador = impressao;
        }

        // Confere os dois códigos: assim a mesma nota copiada de outra versão da página
        // também é reconhecida
        if (notas.existsByUsuarioIdAndIdentificador(usuarioId, identificador)
                || notas.existsByUsuarioIdAndIdentificador(usuarioId, impressao)) {
            throw new NotaJaRegistradaException();
        }

        Mercado mercado = obterOuCriarMercado(lida.getMercado());
        // Sem a linha "Valor total" (nota sem desconto), o total é o valor a pagar
        BigDecimal valorTotal = lida.getValorTotal() != null ? lida.getValorTotal() : lida.getValorAPagar();

        Nota nota = new Nota(usuarios.getReferenceById(usuarioId), mercado, identificador, chave, data,
                valorTotal, lida.getDesconto(), lida.getValorAPagar());
        for (ItemLido item : lida.getItens()) {
            nota.adicionarItem(new ItemNota(item.getNome(), item.getNomeNormalizado(), item.getCodigo(),
                    item.getQuantidade(), item.getUnidade(), item.getPrecoUnitario(), item.getTotal()));
        }

        Nota salva;
        try {
            salva = notas.saveAndFlush(nota);
        } catch (DataIntegrityViolationException e) {
            // Dois envios ao mesmo tempo: quem decide é a restrição única do banco
            if (causaEhNotaDuplicada(e)) {
                throw new NotaJaRegistradaException();
            }
            throw e;
        }
        return new NotaSalvaResponse(salva.getId(), mercado.getNome(), salva.getDataCompra(),
                salva.getValorTotal(), salva.getDesconto(), salva.getValorAPagar(), salva.getItens().size());
    }

    // Tudo o que impede de gravar. Se a lista voltar vazia, a nota pode ser salva.
    private static List<String> conferir(NotaLida lida, String chave, LocalDate data) {
        // Começa pelos avisos do leitor: contagem de itens, soma dos itens, total menos
        // desconto...
        List<String> motivos = new ArrayList<>(lida.getAvisos());

        MercadoLido mercado = lida.getMercado();
        String cnpj = mercado.getCnpj();
        if (cnpj == null || !cnpj.matches("\\d{14}")) {
            motivos.add("Não encontrei um CNPJ válido do mercado no texto.");
        } else if (chave != null && !chave.substring(6, 20).equals(cnpj)) {
            motivos.add("A chave da nota não bate com o CNPJ do texto: o texto pode ser de outra nota.");
        }
        if (mercado.getNome() == null) {
            motivos.add("Não encontrei o nome do mercado no texto.");
        }
        if (lida.getValorAPagar() == null) {
            motivos.add("Não encontrei o valor a pagar no texto.");
        }

        if (data == null) {
            motivos.add("Informe a data da compra: ela não veio no texto.");
        } else {
            if (data.isAfter(LocalDate.now())) {
                motivos.add("A data da compra não pode ser no futuro.");
            }
            // As posições 2 a 5 da chave são o ano e o mês da emissão (AAMM)
            if (chave != null && !chave.substring(2, 6).equals(anoMes(data))) {
                motivos.add("A data informada não bate com o mês da chave da nota.");
            }
        }

        motivos.addAll(limites(lida));
        return motivos;
    }

    // Protege o banco de valores que não cabem nas colunas (e de textos montados
    // para quebrar o sistema)
    private static List<String> limites(NotaLida lida) {
        List<String> problemas = new ArrayList<>();
        for (ItemLido item : lida.getItens()) {
            boolean textoLongo = item.getNome().length() > MAX_NOME_ITEM
                    || (item.getCodigo() != null && item.getCodigo().length() > MAX_CODIGO_ITEM)
                    || item.getUnidade().length() > MAX_UNIDADE;
            boolean valorEstranho = item.getQuantidade().signum() <= 0
                    || item.getQuantidade().compareTo(LIMITE_QUANTIDADE) >= 0
                    || item.getPrecoUnitario().compareTo(LIMITE_DINHEIRO) >= 0
                    || item.getTotal().compareTo(LIMITE_DINHEIRO) >= 0;
            if (textoLongo) {
                problemas.add("Item com texto longo demais: " + resumo(item.getNome()));
            } else if (valorEstranho) {
                problemas.add("Item com quantidade ou valor fora do esperado: " + resumo(item.getNome()));
            }
        }
        if (lida.getValorAPagar() != null && lida.getValorAPagar().compareTo(LIMITE_DINHEIRO) >= 0) {
            problemas.add("Valor da nota fora do esperado.");
        }
        return problemas;
    }

    private Mercado obterOuCriarMercado(MercadoLido lido) {
        return mercados.findByCnpj(lido.getCnpj())
                .orElseGet(() -> mercados.save(
                        new Mercado(lido.getCnpj(), cortar(lido.getNome(), 200), cortar(lido.getEndereco(), 300))));
    }

    private static String anoMes(LocalDate data) {
        return String.format("%02d%02d", data.getYear() % 100, data.getMonthValue());
    }

    private static String resumo(String texto) {
        return cortar(texto, 40);
    }

    private static String cortar(String texto, int maximo) {
        return texto == null || texto.length() <= maximo ? texto : texto.substring(0, maximo);
    }

    private static boolean causaEhNotaDuplicada(DataIntegrityViolationException e) {
        String mensagem = e.getMostSpecificCause().getMessage();
        return mensagem != null && mensagem.contains("uq_nota_usuario_identificador");
    }
}