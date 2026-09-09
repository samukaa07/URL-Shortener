package br.com.urlshortener.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Le a propriedade short-url.base-url do aplicacao.properties.
 * Se por algum motivo o arquivo nao for encontrado, cai num valor padrao
 * pra aplicacao nao quebrar.
 */
public final class ConfiguracaoApp {

    private static final String VALOR_PADRAO = "http://localhost:8080/url-shortener";
    private static final String URL_BASE = carregarUrlBase();

    private ConfiguracaoApp() {
    }

    public static String obterUrlBase() {
        return URL_BASE;
    }

    private static String carregarUrlBase() {
        Properties propriedades = new Properties();
        try (InputStream in = ConfiguracaoApp.class.getClassLoader()
                .getResourceAsStream("aplicacao.properties")) {
            if (in == null) {
                return VALOR_PADRAO;
            }
            propriedades.load(in);
            return propriedades.getProperty("short-url.base-url", VALOR_PADRAO);
        } catch (IOException e) {
            return VALOR_PADRAO;
        }
    }
}
