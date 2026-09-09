package br.com.urlshortener.dto;

/**
 * Corpo esperado no POST /api/urls.
 * O alias e opcional - se vier nulo ou em branco, o sistema gera o codigo sozinho.
 */
public class CriarUrlRequisicao {

    private String url;
    private String alias;

    public CriarUrlRequisicao() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
