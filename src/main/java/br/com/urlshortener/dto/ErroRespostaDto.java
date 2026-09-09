package br.com.urlshortener.dto;

/**
 * Formato padrao de erro devolvido pela API.
 * Ex: { "status": 409, "mensagem": "Alias 'google' ja esta em uso." }
 */
public class ErroRespostaDto {

    private int status;
    private String mensagem;

    public ErroRespostaDto() {
    }

    public ErroRespostaDto(int status, String mensagem) {
        this.status = status;
        this.mensagem = mensagem;
    }

    public int getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }
}
