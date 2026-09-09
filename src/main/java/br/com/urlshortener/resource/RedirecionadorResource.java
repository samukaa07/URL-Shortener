package br.com.urlshortener.resource;

import br.com.urlshortener.service.EncurtadorService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * Redireciona quem acessa a URL curta pra URL original.
 */
@Path("/{codigo}")
public class RedirecionadorResource {

    @Inject
    private EncurtadorService encurtadorService;

    @GET
    public Response redirecionar(@PathParam("codigo") String codigo) {
        String urlOriginal = encurtadorService.resolverUrl(codigo);
        return Response.status(Response.Status.FOUND).location(URI.create(urlOriginal)).build();
    }
}
