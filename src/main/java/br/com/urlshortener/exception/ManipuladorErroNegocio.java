package br.com.urlshortener.exception;

import br.com.urlshortener.dto.ErroRespostaDto;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converte qualquer ErroDeNegocioException (400/404/409) num JSON padronizado,
 * usando o status ja definido em cada excecao.
 */
@Provider
public class ManipuladorErroNegocio implements ExceptionMapper<ErroDeNegocioException> {

    @Override
    public Response toResponse(ErroDeNegocioException excecao) {
        ErroRespostaDto corpo = new ErroRespostaDto(excecao.getStatus(), excecao.getMessage());
        return Response.status(excecao.getStatus())
                .entity(corpo)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
