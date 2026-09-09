package br.com.urlshortener.resource;

import br.com.urlshortener.dto.CriarUrlRequisicao;
import br.com.urlshortener.dto.UrlRespostaDto;
import br.com.urlshortener.service.EncurtadorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Testa o UrlResource isolado, com o service mockado. Nao sobe container/HTTP
 * de verdade (ver decisao no README), mas garante que o resource devolve o
 * status e o corpo certos a partir do que o service retorna.
 */
@ExtendWith(MockitoExtension.class)
class UrlResourceTest {

    @Mock
    private EncurtadorService encurtadorService;

    @InjectMocks
    private UrlResource urlResource;

    @Test
    void deveDevolver201ComShortUrlQuandoServiceCriaComSucesso() {
        CriarUrlRequisicao requisicao = new CriarUrlRequisicao();
        requisicao.setUrl("https://www.google.com");
        UrlRespostaDto respostaEsperada = new UrlRespostaDto("http://localhost:8080/url-shortener/aB72xK");
        when(encurtadorService.criarUrlCurta(requisicao)).thenReturn(respostaEsperada);

        Response resposta = urlResource.criar(requisicao);

        assertEquals(201, resposta.getStatus());
        assertEquals(respostaEsperada, resposta.getEntity());
    }
}
