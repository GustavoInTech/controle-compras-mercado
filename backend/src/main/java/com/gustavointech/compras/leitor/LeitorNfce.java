package com.gustavointech.compras.leitor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê o texto copiado da página da NFC-e (SEFAZ-RJ) e devolve os dados
 * organizados.
 * Java puro: não depende do Spring. Não guarda o texto original nem o CPF do
 * consumidor.
 */
public class LeitorNfce {

    // Um item da nota: nome, código, quantidade, unidade, valor unitário e valor
    // total
    private static final Pattern ITEM = Pattern.compile(
            "^([^\\n]+?)\\s*\\(Código:\\s*([^)]*?)\\s*\\)\\s*Qtde\\.:\\s*([\\d.,]+)\\s*UN:\\s*(\\S+)"
                    + "\\s*Vl\\.\\s*Unit\\.:\\s*([\\d.,]+)\\s*Vl\\.\\s*Total\\s*([\\d.,]+)",
            Pattern.MULTILINE);
    private static final Pattern CNPJ = Pattern.compile("CNPJ:\\s*([\\d./-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern QTD_ITENS = Pattern.compile("Qtd\\. total de itens:\\s*(\\d+)");
    private static final Pattern VALOR_TOTAL = Pattern.compile("Valor total R\\$:\\s*([\\d.,]+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DESCONTO = Pattern.compile("Descontos?\\s*R\\$:\\s*([\\d.,]+)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern VALOR_A_PAGAR = Pattern.compile("Valor a pagar R\\$:\\s*([\\d.,]+)");
    private static final Pattern EMISSAO = Pattern.compile("Emiss[ãa]o:\\s*(\\d{2})/(\\d{2})/(\\d{4})");
    private static final Pattern CHAVE = Pattern.compile("Chave de acesso:\\s*([\\d ]{44,60})");
    private static final Pattern PAGAMENTO = Pattern.compile("^(\\D+?)\\s*([\\d.,]+)$");

    public NotaLida ler(String textoBruto) {
        String texto = textoBruto.replace("\r", "").replace('\u00a0', ' ');
        List<String> linhas = texto.lines().map(String::strip).filter(l -> !l.isEmpty()).toList();
        List<String> avisos = new ArrayList<>();

        MercadoLido mercado = lerMercado(texto, linhas);
        List<ItemLido> itens = lerItens(texto, avisos);
        List<PagamentoLido> pagamentos = lerPagamentos(linhas);

        String qtd = primeiroGrupo(QTD_ITENS, texto);
        Integer quantidadeItensNota = qtd != null ? Integer.valueOf(qtd) : null;

        BigDecimal valorTotal = numeroOuNull(primeiroGrupo(VALOR_TOTAL, texto));
        BigDecimal valorAPagar = numeroOuNull(primeiroGrupo(VALOR_A_PAGAR, texto));
        BigDecimal desconto = numeroOuNull(primeiroGrupo(DESCONTO, texto));
        if (desconto == null) {
            desconto = BigDecimal.ZERO;
        }

        conferir(itens, quantidadeItensNota, valorTotal, desconto, valorAPagar, avisos);

        return new NotaLida(mercado, lerChave(texto), lerData(texto), itens, quantidadeItensNota,
                valorTotal, desconto, valorAPagar, pagamentos, avisos);
    }

    // ---------- Leitura de cada parte do texto ----------

    private static MercadoLido lerMercado(String texto, List<String> linhas) {
        int iCnpj = -1;
        for (int i = 0; i < linhas.size(); i++) {
            if (linhas.get(i).regionMatches(true, 0, "CNPJ:", 0, 5)) {
                iCnpj = i;
                break;
            }
        }
        String nome = iCnpj > 0 ? linhas.get(iCnpj - 1) : null;
        String endereco = (iCnpj >= 0 && iCnpj + 1 < linhas.size()) ? linhas.get(iCnpj + 1) : null;
        String cnpj = primeiroGrupo(CNPJ, texto);
        return new MercadoLido(nome, cnpj != null ? cnpj.replaceAll("\\D", "") : null, endereco);
    }

    private static List<ItemLido> lerItens(String texto, List<String> avisos) {
        List<ItemLido> itens = new ArrayList<>();
        Matcher m = ITEM.matcher(texto);
        while (m.find()) {
            String nome = m.group(1).strip();
            try {
                String codigo = m.group(2).strip();
                itens.add(new ItemLido(
                        nome,
                        normalizarNome(nome),
                        codigo.isEmpty() ? null : codigo,
                        paraNumero(m.group(3)),
                        m.group(4).toUpperCase(Locale.ROOT),
                        paraNumero(m.group(5)),
                        paraNumero(m.group(6))));
            } catch (NumberFormatException e) {
                avisos.add("Não consegui interpretar o item: " + nome);
            }
        }
        return itens;
    }

    private static List<PagamentoLido> lerPagamentos(List<String> linhas) {
        List<PagamentoLido> pagamentos = new ArrayList<>();
        int inicio = -1;
        for (int i = 0; i < linhas.size(); i++) {
            if (linhas.get(i).regionMatches(true, 0, "Forma de pagamento", 0, 18)) {
                inicio = i;
                break;
            }
        }
        if (inicio < 0) {
            return pagamentos;
        }
        for (int i = inicio + 1; i < linhas.size(); i++) {
            Matcher m = PAGAMENTO.matcher(linhas.get(i));
            if (!m.matches()) {
                break; // acabou a lista de pagamentos
            }
            pagamentos.add(new PagamentoLido(m.group(1).strip(), paraNumero(m.group(2))));
        }
        return pagamentos;
    }

    private static LocalDate lerData(String texto) {
        Matcher m = EMISSAO.matcher(texto);
        if (!m.find()) {
            return null;
        }
        try {
            return LocalDate.of(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(1)));
        } catch (DateTimeException e) {
            return null;
        }
    }

    private static String lerChave(String texto) {
        Matcher m = CHAVE.matcher(texto);
        if (!m.find()) {
            return null;
        }
        String chave = m.group(1).replaceAll("\\s", "");
        return chave.length() == 44 ? chave : null;
    }

    // ---------- Conferências: é aqui que a leitura se prova certa ----------

    private static void conferir(List<ItemLido> itens, Integer quantidadeItensNota, BigDecimal valorTotal,
            BigDecimal desconto, BigDecimal valorAPagar, List<String> avisos) {
        if (itens.isEmpty()) {
            avisos.add("Nenhum item foi reconhecido. O texto colado é de uma nota?");
        }
        if (quantidadeItensNota != null && quantidadeItensNota != itens.size()) {
            avisos.add("A nota diz " + quantidadeItensNota + " itens, mas li " + itens.size() + ".");
        }

        BigDecimal soma = itens.stream().map(ItemLido::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        // Com desconto, a soma dos itens deve bater com o "Valor total"; sem ele, com o
        // "Valor a pagar"
        BigDecimal alvo = valorTotal != null ? valorTotal : valorAPagar;
        if (alvo == null) {
            avisos.add("Não encontrei o valor total da nota no texto.");
        } else if (soma.compareTo(alvo) != 0) {
            avisos.add("A soma dos itens (R$ " + formatar(soma) + ") é diferente do valor da nota (R$ "
                    + formatar(alvo) + "). Pode ter item não lido.");
        }
        if (valorTotal != null && valorAPagar != null
                && valorTotal.subtract(desconto).compareTo(valorAPagar) != 0) {
            avisos.add("Valor total menos descontos não bate com o valor a pagar.");
        }
    }

    // ---------- Ferramentas pequenas ----------

    private static String primeiroGrupo(Pattern padrao, String texto) {
        Matcher m = padrao.matcher(texto);
        return m.find() ? m.group(1) : null;
    }

    // "1.234,56" vira 1234.56 (BigDecimal: nunca double para dinheiro)
    private static BigDecimal paraNumero(String s) {
        return new BigDecimal(s.replace(".", "").replace(",", "."));
    }

    private static BigDecimal numeroOuNull(String s) {
        if (s == null) {
            return null;
        }
        try {
            return paraNumero(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String formatar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    // "ARROZ T1 5KG" e "arroz t1 5 kg" ficam comparáveis: maiúsculas, sem acento,
    // sem pontuação
    static String normalizarNome(String nome) {
        String semAcento = Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9 ]", " ").replaceAll("\\s+", " ").strip();
    }
}