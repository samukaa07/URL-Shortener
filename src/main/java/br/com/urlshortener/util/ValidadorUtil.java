package br.com.urlshortener.util;

import java.util.regex.Pattern;

/**
 * Validacoes simples de formato para URL e alias.
 * Nao tem pretensao de ser um validador de URL "perfeito" (RFC completa),
 * so o suficiente pra barrar entradas obviamente invalidas.
 */
public final class ValidadorUtil {

    private static final Pattern PADRAO_URL = Pattern.compile("^https?://.+\\..+");
    private static final Pattern PADRAO_ALIAS = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private ValidadorUtil() {
    }

    public static boolean urlValida(String url) {
        return url != null && !url.trim().isEmpty() && PADRAO_URL.matcher(url.trim()).matches();
    }

    public static boolean aliasValido(String alias) {
        return alias != null && PADRAO_ALIAS.matcher(alias).matches();
    }
}
