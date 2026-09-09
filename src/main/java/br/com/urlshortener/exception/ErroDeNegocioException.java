package br.com.urlshortener.exception;

import javax.ejb.ApplicationException;

/**
 * Base para as excecoes de regra de negocio da aplicacao.
 * Cada uma carrega o status HTTP que deve ser devolvido pro cliente.
 *
 * @ApplicationException e essencial aqui: o EncurtadorService e um EJB @Stateless,
 * e por padrao o container embrulha qualquer RuntimeException lancada de dentro de um
 * EJB numa EJBException (tratando como erro de sistema). Isso faria o ManipuladorErroNegocio
 * nunca "ver" a excecao original, e o cliente sempre receberia 500 em vez de 400/404/409.
 * Com essa anotacao, o container repassa a excecao de negocio sem embrulhar.
 */
@ApplicationException(rollback = true)
public abstract class ErroDeNegocioException extends RuntimeException {

    private final int status;

    protected ErroDeNegocioException(int status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
