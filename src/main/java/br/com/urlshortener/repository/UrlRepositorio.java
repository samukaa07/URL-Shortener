package br.com.urlshortener.repository;

import br.com.urlshortener.entity.UrlCurta;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Optional;

/**
 * Acesso a dados da entidade UrlCurta. Nao tem regra de negocio aqui,
 * so consultas e persistencia.
 */
@Stateless
public class UrlRepositorio {

    @PersistenceContext(unitName = "url-shortener-pu")
    private EntityManager em;

    public void salvar(UrlCurta urlCurta) {
        em.persist(urlCurta);
    }

    public Optional<UrlCurta> buscarPorCodigo(String codigo) {
        TypedQuery<UrlCurta> query = em.createQuery(
                "select u from UrlCurta u where u.codigo = :codigo", UrlCurta.class);
        query.setParameter("codigo", codigo);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public boolean existePorCodigo(String codigo) {
        TypedQuery<Long> query = em.createQuery(
                "select count(u) from UrlCurta u where u.codigo = :codigo", Long.class);
        query.setParameter("codigo", codigo);
        return query.getSingleResult() > 0;
    }
}
