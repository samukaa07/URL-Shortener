package br.com.urlshortener.exception;

import javax.ws.rs.core.Response;

/**
 * Disparada quando o codigo/alias buscado no redirect nao existe.
 */
public class CodigoNaoEncontradoException extends ErroDeNegocioException {

    public CodigoNaoEncontradoException(String codigo) {
        super(Response.Status.NOT_FOUND.getStatusCode(), "Codigo '" + codigo + "' nao encontrado.");
    }
}
