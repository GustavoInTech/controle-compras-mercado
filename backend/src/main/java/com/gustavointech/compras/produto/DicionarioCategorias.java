package com.gustavointech.compras.produto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Dicionário de palavras que sugere a categoria de um item pelo nome (ADR 0005, decisão 4).
// Java puro, sem Spring, como o leitor: é fácil de testar com notas reais.
//
// Como funciona: o nome já vem normalizado (maiúsculas, sem acento nem pontuação). Cada categoria tem uma lista de
// palavras ou frases; vale a PRIMEIRA categoria, na ordem abaixo, que reconhecer o nome. Só vale a palavra
// inteira: "SAL" não reconhece "SALSICHA".
//
// Ele erra, e isso é esperado: o usuário corrige com um toque (RF17).
final class DicionarioCategorias {

    // A ORDEM IMPORTA: as frases mais específicas ficam antes (por exemplo, "AGUA
    // SANITARIA" é Limpeza,
    // e por isso Limpeza vem antes de Bebidas, que reconhece "AGUA"; "SAZON" vem
    // antes de Carnes, por causa de "BACON").
    private static final Map<String, List<String>> REGRAS = new LinkedHashMap<>();

    static {
        REGRAS.put("Limpeza", List.of("DETERGENTE", "SABAO", "AMACIANTE", "DESINFETANTE", "AGUA SANITARIA",
                "ESPONJA", "ALVEJANTE", "MULTIUSO", "LIMPADOR", "LAVA ROUPAS", "LIXO", "VASSOURA", "PANO"));
        REGRAS.put("Higiene", List.of("SHAMPOO", "CONDICIONADOR", "SABONETE", "CREME DENTAL", "DESODORANTE",
                "PAPEL HIGIENICO", "ABSORVENTE", "FRALDA", "COTONETE", "ESCOVA DENTAL"));
        REGRAS.put("Congelados", List.of("BAT CONG", "BATATA CONG", "BATATA CONGELADA", "PIZZA", "PIZ", "HAMB",
                "HAMBURGUER", "NUGGETS", "CHIKEN BITS", "CHICKEN BITS", "SORVETE", "PICOLE", "LASANHA",
                "EMPANADO", "ALMONDEGA"));
        REGRAS.put("Bebidas", List.of("REFRIGERANTE", "REFRI", "GUARANA", "GUARAVITA", "SUCO", "AGUA", "CERVEJA",
                "VINHO", "ENERGETICO", "ISOTONICO", "ACH", "ACHOCOLATADO"));
        REGRAS.put("Laticínios", List.of("LEITE", "LTE", "L COND", "L FER", "CR LEITE", "CR CHANT", "CREME DE LEITE",
                "QUEIJO", "REQUEIJAO", "IOGURTE", "YOPRO", "MANTEIGA", "MARG", "MARGARINA", "NATA", "MUSSARELA"));
        REGRAS.put("Padaria", List.of("PAO", "PAES", "BOLO", "TORRADA", "BISNAGUINHA", "ANA MARIA", "BROA"));
        REGRAS.put("Mercearia", List.of("ARROZ", "FEIJAO", "MACARRAO", "MASSA", "OLEO", "ACUCAR", "SAL", "FARINHA",
                "CAFE", "BISCOITO", "BIS", "BOLACHA", "SALGADINHO", "SALG", "PRINGLES", "CHOCOLATE", "CHOC", "KINDER",
                "SNICKERS", "SARD", "SARDINHA", "ATUM", "MILHO", "MOLHO", "MAIONESE", "MN HELLM", "HELLM", "KETCHUP",
                "SUCRILHOS", "CEREAL", "AVEIA", "GELEIA", "PO SORV", "SAZON", "TEMPERO", "VINAGRE", "OVO", "OVOS",
                "FINI", "BALA", "ERVILHA"));
        REGRAS.put("Carnes", List.of("CARNE", "BOVINO", "CONTR", "CONTRAFILE", "COSTELA", "ACEM", "PATINHO",
                "ALCATRA", "PICANHA", "MAMINHA", "FRANGO", "ASA", "COXA", "SOBRECOXA", "FILE PEITO", "PEITO",
                "LINGUICA", "LING", "SALSICHA", "BACON", "PERNIL", "SUINO", "PEIXE", "TILAPIA", "CAMARAO"));
        REGRAS.put("Hortifruti", List.of("BATATA", "BETERRABA", "CEBOLA", "CENOURA", "TOMATE", "ALFACE", "BANANA",
                "MACA", "LARANJA", "LIMAO", "ABOBRINHA", "ALHO", "MELANCIA", "MAMAO", "UVA", "PIMENTAO", "CHUCHU",
                "REPOLHO", "COUVE", "BROCOLIS", "ABACAXI", "MANGA", "MELAO", "PEPINO", "MANDIOCA", "INHAME",
                "ABOBORA", "COENTRO", "CHEIRO VERDE", "MORANGO"));
    }

    private DicionarioCategorias() {
    }

    // O nome da categoria sugerida (igual ao da tabela categoria), ou vazio quando
    // o dicionário não conhece o nome
    static Optional<String> sugerir(String nomeNormalizado) {
        if (nomeNormalizado == null) {
            return Optional.empty();
        }
        // Com um espaço de cada lado, " SAL " só aparece quando SAL é uma palavra
        // inteira
        String texto = " " + nomeNormalizado + " ";
        for (Map.Entry<String, List<String>> regra : REGRAS.entrySet()) {
            for (String frase : regra.getValue()) {
                if (texto.contains(" " + frase + " ")) {
                    return Optional.of(regra.getKey());
                }
            }
        }
        return Optional.empty();
    }
}