package br.com.urlshortener.config;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Liga o JAX-RS na raiz do contexto da aplicacao, entao os endpoints
 * ficam em /url-shortener/api/urls e /url-shortener/{codigo}.
 */
@ApplicationPath("/")
public class JaxRsAplicacao extends Application {
}
