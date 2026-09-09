package br.com.urlshortener.util;

import javax.enterprise.context.ApplicationScoped;
import java.security.SecureRandom;

/**
 * Gera codigos curtos aleatorios (letras maiusculas, minusculas e numeros).
 * Nao tem nenhuma logica de unicidade aqui - isso e responsabilidade do service,
 * que confere no banco antes de usar o codigo gerado.
 */
@ApplicationScoped
public class GeradorDeCodigo {

    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TAMANHO_CODIGO = 6;

    private final SecureRandom random = new SecureRandom();

    public String gerar() {
        StringBuilder codigo = new StringBuilder(TAMANHO_CODIGO);
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            int posicao = random.nextInt(CARACTERES.length());
            codigo.append(CARACTERES.charAt(posicao));
        }
        return codigo.toString();
    }
}
