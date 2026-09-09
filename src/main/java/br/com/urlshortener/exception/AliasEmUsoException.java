package br.com.urlshortener.exception;

import javax.ws.rs.core.Response;

/**
 * Disparada quando o alias escolhido pelo usuario ja esta em uso por outra URL.
 */
public class AliasEmUsoException extends ErroDeNegocioException {

    public AliasEmUsoException(String alias) {
        super(Response.Status.CONFLICT.getStatusCode(), "Alias '" + alias + "' ja esta em uso.");
    }
}
