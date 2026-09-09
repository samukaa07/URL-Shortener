package br.com.urlshortener.service;

import br.com.urlshortener.dto.CriarUrlRequisicao;
import br.com.urlshortener.dto.UrlRespostaDto;
import br.com.urlshortener.entity.UrlCurta;
import br.com.urlshortener.exception.AliasEmUsoException;
import br.com.urlshortener.exception.AliasInvalidoException;
import br.com.urlshortener.exception.CodigoNaoEncontradoException;
import br.com.urlshortener.exception.UrlInvalidaException;
import br.com.urlshortener.repository.UrlRepositorio;
import br.com.urlshortener.util.GeradorDeCodigo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncurtadorServiceTest {

    @Mock
    private UrlRepositorio urlRepositorio;

    @Mock
    private GeradorDeCodigo geradorDeCodigo;

    @InjectMocks
    private EncurtadorService service;

    private CriarUrlRequisicao requisicao;

    @BeforeEach
    void setUp() {
        requisicao = new CriarUrlRequisicao();
    }

    @Test
    void deveCriarUrlCurtaSemAlias() {
        requisicao.setUrl("https://www.google.com");
        when(geradorDeCodigo.gerar()).thenReturn("aB72xK");
        when(urlRepositorio.existePorCodigo("aB72xK")).thenReturn(false);
        doNothing().when(urlRepositorio).salvar(any(UrlCurta.class));

        UrlRespostaDto resposta = service.criarUrlCurta(requisicao);

        assertTrue(resposta.getShortUrl().endsWith("/aB72xK"));
    }

    @Test
    void deveCriarUrlCurtaComAliasDisponivel() {
        requisicao.setUrl("https://www.google.com");
        requisicao.setAlias("google");
        when(urlRepositorio.existePorCodigo("google")).thenReturn(false);
        doNothing().when(urlRepositorio).salvar(any(UrlCurta.class));

        UrlRespostaDto resposta = service.criarUrlCurta(requisicao);

        assertTrue(resposta.getShortUrl().endsWith("/google"));
    }

    @Test
    void deveRejeitarAliasJaUtilizado() {
        requisicao.setUrl("https://www.google.com");
        requisicao.setAlias("google");
        when(urlRepositorio.existePorCodigo("google")).thenReturn(true);

        assertThrows(AliasEmUsoException.class, () -> service.criarUrlCurta(requisicao));
    }

    @Test
    void deveRejeitarAliasComCaracteresInvalidos() {
        requisicao.setUrl("https://www.google.com");
        requisicao.setAlias("meu site");

        assertThrows(AliasInvalidoException.class, () -> service.criarUrlCurta(requisicao));
    }

    @Test
    void deveRejeitarUrlInvalida() {
        requisicao.setUrl("nao-e-uma-url");

        assertThrows(UrlInvalidaException.class, () -> service.criarUrlCurta(requisicao));
    }

    @Test
    void deveRejeitarUrlVazia() {
        requisicao.setUrl("");

        assertThrows(UrlInvalidaException.class, () -> service.criarUrlCurta(requisicao));
    }

    @Test
    void deveResolverUrlQuandoCodigoExiste() {
        UrlCurta urlCurta = new UrlCurta("google", "https://www.google.com");
        when(urlRepositorio.buscarPorCodigo("google")).thenReturn(Optional.of(urlCurta));

        String urlOriginal = service.resolverUrl("google");

        assertEquals("https://www.google.com", urlOriginal);
    }

    @Test
    void deveLancarErroQuandoCodigoNaoExiste() {
        when(urlRepositorio.buscarPorCodigo("inexistente")).thenReturn(Optional.empty());

        assertThrows(CodigoNaoEncontradoException.class, () -> service.resolverUrl("inexistente"));
    }

    @Test
    void deveTentarNovamenteQuandoCodigoGeradoJaExiste() {
        requisicao.setUrl("https://www.google.com");
        // primeira tentativa colide, segunda ja emplaca
        when(geradorDeCodigo.gerar()).thenReturn("colide1", "livre22");
        when(urlRepositorio.existePorCodigo("colide1")).thenReturn(true);
        when(urlRepositorio.existePorCodigo("livre22")).thenReturn(false);
        doNothing().when(urlRepositorio).salvar(any(UrlCurta.class));

        UrlRespostaDto resposta = service.criarUrlCurta(requisicao);

        assertTrue(resposta.getShortUrl().endsWith("/livre22"));
    }

    /**
     * Simula varias requisicoes concorrentes sem alias usando um GeradorDeCodigo
     * de verdade (nao mockado) e um repositorio falso apoiado num Set thread-safe.
     * Como a geracao+checagem de colisao roda dentro de um bloco sincronizado no
     * service, o resultado esperado e: todos os codigos gerados saem unicos.
     */
    @Test
    void devegarantirCodigosUnicosSobConcorrencia() throws InterruptedException {
        GeradorDeCodigo geradorReal = new GeradorDeCodigo();
        Set<String> codigosUsados = ConcurrentHashMap.newKeySet();

        when(geradorDeCodigo.gerar()).thenAnswer(invocacao -> geradorReal.gerar());
        when(urlRepositorio.existePorCodigo(anyString()))
                .thenAnswer(invocacao -> codigosUsados.contains(invocacao.getArgument(0)));

        int totalThreads = 30;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch largada = new CountDownLatch(1);
        Set<String> shortUrlsGeradas = Collections.synchronizedSet(new java.util.HashSet<>());

        for (int i = 0; i < totalThreads; i++) {
            executor.submit(() -> {
                try {
                    largada.await();
                    CriarUrlRequisicao req = new CriarUrlRequisicao();
                    req.setUrl("https://www.exemplo.com");
                    UrlRespostaDto resposta = service.criarUrlCurta(req);
                    codigosUsados.add(resposta.getShortUrl().substring(resposta.getShortUrl().lastIndexOf('/') + 1));
                    shortUrlsGeradas.add(resposta.getShortUrl());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        largada.countDown();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(totalThreads, shortUrlsGeradas.size());
    }
}
