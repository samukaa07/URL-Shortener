package br.com.urlshortener.util;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeradorDeCodigoTest {

    private final GeradorDeCodigo gerador = new GeradorDeCodigo();

    @RepeatedTest(20)
    void deveGerarCodigoComSeisCaracteresAlfanumericos() {
        String codigo = gerador.gerar();

        assertEquals(6, codigo.length());
        assertTrue(codigo.matches("^[a-zA-Z0-9]{6}$"), "codigo gerado: " + codigo);
    }

    @Test
    void deveGerarCodigosDiferentesEntreChamadas() {
        String codigo1 = gerador.gerar();
        String codigo2 = gerador.gerar();

        // teoricamente poderiam colidir, mas com 62^6 combinacoes a chance e desprezivel
        assertTrue(!codigo1.equals(codigo2));
    }
}
