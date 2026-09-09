package br.com.urlshortener.exception;

import javax.ws.rs.core.Response;

/**
 * Disparada quando o alias informado tem caracteres nao permitidos.
 */
public class AliasInvalidoException extends ErroDeNegocioException {

    public AliasInvalidoException(String mensagem) {
        super(Response.Status.BAD_REQUEST.getStatusCode(), mensagem);
    }
}
