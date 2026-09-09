package br.com.urlshortener.resource;

import br.com.urlshortener.dto.CriarUrlRequisicao;
import br.com.urlshortener.dto.UrlRespostaDto;
import br.com.urlshortener.service.EncurtadorService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * API de criacao de URLs curtas. So recebe a requisicao, chama o service
 * e devolve a resposta - a regra de negocio toda mora no EncurtadorService.
 */
@Path("/api/urls")
public class UrlResource {

    @Inject
    private EncurtadorService encurtadorService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response criar(CriarUrlRequisicao requisicao) {
        UrlRespostaDto resposta = encurtadorService.criarUrlCurta(requisicao);
        return Response.status(Response.Status.CREATED).entity(resposta).build();
    }
}
