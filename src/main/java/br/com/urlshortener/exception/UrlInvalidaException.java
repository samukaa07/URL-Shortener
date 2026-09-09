package br.com.urlshortener.exception;

import javax.ws.rs.core.Response;

/**
 * Disparada quando a URL informada esta vazia ou nao tem um formato valido.
 */
public class UrlInvalidaException extends ErroDeNegocioException {

    public UrlInvalidaException(String mensagem) {
        super(Response.Status.BAD_REQUEST.getStatusCode(), mensagem);
    }
}
