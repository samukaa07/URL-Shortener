package br.com.urlshortener.service;

import br.com.urlshortener.config.ConfiguracaoApp;
import br.com.urlshortener.dto.CriarUrlRequisicao;
import br.com.urlshortener.dto.UrlRespostaDto;
import br.com.urlshortener.entity.UrlCurta;
import br.com.urlshortener.exception.AliasEmUsoException;
import br.com.urlshortener.exception.AliasInvalidoException;
import br.com.urlshortener.exception.CodigoNaoEncontradoException;
import br.com.urlshortener.exception.UrlInvalidaException;
import br.com.urlshortener.repository.UrlRepositorio;
import br.com.urlshortener.util.GeradorDeCodigo;
import br.com.urlshortener.util.ValidadorUtil;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Optional;

/**
 * Regra de negocio do encurtador. O resource so chama esses metodos,
 * nao tem nada de validacao/persistencia espalhado por la.
 */
@Stateless
public class EncurtadorService {

    // trava usada so na hora de gerar+conferir o codigo automatico,
    // que e o trecho que o desafio pede pra ser processado um por vez.
    private static final Object TRAVA_GERACAO = new Object();

    @Inject
    private UrlRepositorio urlRepositorio;

    @Inject
    private GeradorDeCodigo geradorDeCodigo;

    public UrlRespostaDto criarUrlCurta(CriarUrlRequisicao requisicao) {
        String url = requisicao == null ? null : requisicao.getUrl();
        String alias = requisicao == null ? null : requisicao.getAlias();

        if (!ValidadorUtil.urlValida(url)) {
            throw new UrlInvalidaException("A URL informada e invalida ou esta vazia.");
        }

        String codigo = (alias != null && !alias.trim().isEmpty())
                ? usarAlias(alias.trim())
                : gerarCodigoDisponivel();

        UrlCurta urlCurta = new UrlCurta(codigo, url.trim());
        urlRepositorio.salvar(urlCurta);

        String urlCompleta = ConfiguracaoApp.obterUrlBase() + "/" + codigo;
        return new UrlRespostaDto(urlCompleta);
    }

    public String resolverUrl(String codigo) {
        Optional<UrlCurta> encontrada = urlRepositorio.buscarPorCodigo(codigo);
        if (!encontrada.isPresent()) {
            throw new CodigoNaoEncontradoException(codigo);
        }
        return encontrada.get().getUrlOriginal();
    }

    private String usarAlias(String alias) {
        if (!ValidadorUtil.aliasValido(alias)) {
            throw new AliasInvalidoException(
                    "Alias invalido. Use apenas letras, numeros, hifen ou underscore.");
        }
        if (urlRepositorio.existePorCodigo(alias)) {
            throw new AliasEmUsoException(alias);
        }
        return alias;
    }

    /**
     * Gera um codigo automatico garantindo que nao existe outro igual.
     * Sincronizado de proposito: como o requisito pede que essa geracao
     * seja "uma por vez", trancamos so este trecho critico (gerar + checar
     * colisao), sem travar o resto do service nem os outros metodos.
     */
    private String gerarCodigoDisponivel() {
        synchronized (TRAVA_GERACAO) {
            String candidato;
            do {
                candidato = geradorDeCodigo.gerar();
            } while (urlRepositorio.existePorCodigo(candidato));
            return candidato;
        }
    }
}
