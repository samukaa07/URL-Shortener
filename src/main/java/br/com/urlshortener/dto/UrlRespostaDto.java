package br.com.urlshortener.dto;

/**
 * Resposta devolvida quando uma URL curta e criada com sucesso.
 */
public class UrlRespostaDto {

    private String shortUrl;

    public UrlRespostaDto() {
    }

    public UrlRespostaDto(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }
}
