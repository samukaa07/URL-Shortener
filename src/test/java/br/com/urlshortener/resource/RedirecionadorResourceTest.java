package br.com.urlshortener.resource;

import br.com.urlshortener.exception.CodigoNaoEncontradoException;
import br.com.urlshortener.service.EncurtadorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Testa o redirecionamento isolado, com o service mockado.
 */
@ExtendWith(MockitoExtension.class)
class RedirecionadorResourceTest {

    @Mock
    private EncurtadorService encurtadorService;

    @InjectMocks
    private RedirecionadorResource redirecionadorResource;

    @Test
    void deveDevolver302ComLocationApontandoParaUrlOriginal() {
        when(encurtadorService.resolverUrl("google")).thenReturn("https://www.google.com");

        Response resposta = redirecionadorResource.redirecionar("google");

        assertEquals(302, resposta.getStatus());
        assertEquals("https://www.google.com", resposta.getLocation().toString());
    }

    @Test
    void devePropagarErroQuandoCodigoNaoExiste() {
        when(encurtadorService.resolverUrl("inexistente"))
                .thenThrow(new CodigoNaoEncontradoException("inexistente"));

        // quem transforma isso em 404 e o ManipuladorErroNegocio (ExceptionMapper);
        // aqui so garantimos que o resource nao engole o erro escondido.
        assertThrows(CodigoNaoEncontradoException.class,
                () -> redirecionadorResource.redirecionar("inexistente"));
    }
}
