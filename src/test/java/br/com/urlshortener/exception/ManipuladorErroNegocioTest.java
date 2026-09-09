package br.com.urlshortener.exception;

import br.com.urlshortener.dto.ErroRespostaDto;
import org.junit.jupiter.api.Test;

import javax.ejb.ApplicationException;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Garante que cada excecao de negocio vira o status HTTP certo e um corpo
 * JSON consistente, sem vazar detalhes internos.
 */
class ManipuladorErroNegocioTest {

    private final ManipuladorErroNegocio manipulador = new ManipuladorErroNegocio();

    @Test
    void aliasEmUsoDeveVirar409() {
        Response resposta = manipulador.toResponse(new AliasEmUsoException("google"));

        assertEquals(409, resposta.getStatus());
        ErroRespostaDto corpo = (ErroRespostaDto) resposta.getEntity();
        assertEquals(409, corpo.getStatus());
        assertEquals("Alias 'google' ja esta em uso.", corpo.getMensagem());
    }

    @Test
    void codigoNaoEncontradoDeveVirar404() {
        Response resposta = manipulador.toResponse(new CodigoNaoEncontradoException("abc123"));

        assertEquals(404, resposta.getStatus());
    }

    @Test
    void urlInvalidaDeveVirar400() {
        Response resposta = manipulador.toResponse(new UrlInvalidaException("URL vazia ou invalida."));

        assertEquals(400, resposta.getStatus());
    }

    /**
     * Sem @ApplicationException, o EJB @Stateless embrulharia essas excecoes numa
     * EJBException e o cliente sempre receberia 500 em vez do status correto.
     * Esse teste existe pra nao deixarem essa anotacao ser removida sem querer.
     */
    @Test
    void erroDeNegocioDeveEstarMarcadoComoApplicationException() {
        assertTrue(ErroDeNegocioException.class.isAnnotationPresent(ApplicationException.class));
    }
}

