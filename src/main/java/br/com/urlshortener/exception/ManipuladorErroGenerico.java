package br.com.urlshortener.exception;

import br.com.urlshortener.dto.ErroRespostaDto;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Rede de seguranca para qualquer excecao que nao seja de negocio.
 * Nunca devolve stacktrace pro cliente, so loga no servidor e responde 500 generico.
 */
@Provider
public class ManipuladorErroGenerico implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(ManipuladorErroGenerico.class.getName());

    @Override
    public Response toResponse(Exception excecao) {
        LOG.log(Level.SEVERE, "Erro inesperado ao processar requisicao", excecao);

        ErroRespostaDto corpo = new ErroRespostaDto(500, "Erro interno no servidor.");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(corpo)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
