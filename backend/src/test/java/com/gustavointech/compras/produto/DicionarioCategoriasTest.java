package com.gustavointech.compras.produto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

// Sem Spring e sem banco: só o dicionário, testado com os nomes das duas notas reais (anonimizadas).
class DicionarioCategoriasTest {

    @Test
    void categorizaOsNomesDasNotasReais() {
        String[][] casos = {
                // Nota do açougue
                { "BETERRABA KG", "Hortifruti" }, { "CEBOLA KG", "Hortifruti" }, { "CENOURA KG", "Hortifruti" },
                { "BATATA KG", "Hortifruti" }, { "LINGUICA PERNIL CG S KG", "Carnes" },
                { "ASA FRANGO CG COPAVEL KG", "Carnes" }, { "COSTELA P CHURRASCO KG", "Carnes" },
                { "CONTR BOVINO KG", "Carnes" },
                // Nota do atacadista
                { "FILE PEITO SEARA 1KG", "Carnes" }, { "CHIKEN BITS REZ 1KG", "Congelados" },
                { "OVO BCO GDE C 20", "Mercearia" }, { "OVO BCO GDE PVC 20UN", "Mercearia" },
                { "BAT CONG CHIC 2KG", "Congelados" }, { "MARG QUALY 1KG C S", "Laticínios" },
                { "HAMB FRIBOI 672G", "Congelados" }, { "ACH PIRAKIDS 200ML", "Bebidas" },
                { "L FER CHAMY 450G TRA", "Laticínios" }, { "L COND ITALAC 395G", "Laticínios" },
                { "CR LEITE PIRAC 200G", "Laticínios" }, { "LTE INT PIRAC 1L C T", "Laticínios" },
                { "B PRINGLES 104G ORIG", "Mercearia" }, { "GUARAVITA GUARANA 1L", "Bebidas" },
                { "CR CHANT 1L GRAN FIN", "Laticínios" }, { "LING FIN PERD 215G", "Carnes" },
                { "PIZZA SEARA 440G MUSSA", "Congelados" }, { "PIZ SEARA 460G CALAB", "Congelados" },
                { "KINDER WHITE T2 39G", "Mercearia" }, { "CHOC SNICKERS 40G PE", "Mercearia" },
                { "C SAZON 32 5G BACON", "Mercearia" }, { "PO SORV APTI 120G CR", "Mercearia" },
                { "SAZON VERM 60G", "Mercearia" }, { "MILHO FUGINI SC 170G", "Mercearia" },
                { "MN HELLM DOY 400G", "Mercearia" }, { "SARD GC 75G OLEO", "Mercearia" },
                { "SARD GC 75G MOLHO", "Mercearia" }, { "LING CALAB PERD 1KG", "Carnes" },
                { "SUCRILHOS 280G ORIG", "Mercearia" }, { "YOPRO 250ML CHO 15G", "Laticínios" },
                { "B FINI TUB 80G ARCO", "Mercearia" }, { "ANA MARIA 70G CEN CH", "Padaria" },
                { "ANA MARIA 70G CHOC", "Padaria" }, { "ANA MARIA 70G BAUN", "Padaria" },
                { "ANA MARIA 70G GOTAS", "Padaria" }, { "BIS LV PG 302 4G BC", "Mercearia" },
                { "SALG DORIT 75G NACHO", "Mercearia" }, { "SALG DORIT 120G NACH", "Mercearia" },
        };

        for (String[] caso : casos) {
            assertEquals(Optional.of(caso[1]), DicionarioCategorias.sugerir(caso[0]), caso[0]);
        }
    }

    @Test
    void nomesSemPistaFicamSemSugestao() {
        // O dicionário não conhece estes: viram "Outros" e o usuário corrige com um
        // toque
        String[] semPista = { "F TR ROSA BCA 1KG", "CARVA VEGETAL ITOROR 3KG", "SACOLA GREEN 40X50",
                "SAC 58X70 BIO VERDE", "SAC ASSA VD BI 48X55" };

        for (String nome : semPista) {
            assertEquals(Optional.empty(), DicionarioCategorias.sugerir(nome), nome);
        }
    }

    @Test
    void soReconhecePalavraInteiraEUsaAOrdemDasCategorias() {
        // "SAL" (Mercearia) não pode reconhecer um pedaço de "SALSICHA"
        assertEquals(Optional.of("Carnes"), DicionarioCategorias.sugerir("SALSICHA HOT DOG KG"));
        // Limpeza vem antes de Bebidas, que reconheceria "AGUA"
        assertEquals(Optional.of("Limpeza"), DicionarioCategorias.sugerir("AGUA SANITARIA 2L"));
        assertEquals(Optional.of("Higiene"), DicionarioCategorias.sugerir("CREME DENTAL COLGATE 90G"));
        // "OVO" só vale como palavra inteira
        assertEquals(Optional.empty(), DicionarioCategorias.sugerir("OVOMALTINE 400G"));
        // Mercearia (SAZON) vem antes de Carnes (BACON): o tempero sabor bacon não é
        // carne
        assertEquals(Optional.of("Mercearia"), DicionarioCategorias.sugerir("C SAZON 32 5G BACON"));
        assertEquals(Optional.empty(), DicionarioCategorias.sugerir(null));
    }
}