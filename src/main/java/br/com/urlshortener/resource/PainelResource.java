package br.com.urlshortener.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Serve a paginazinha de front-end. Optei por devolver o HTML aqui, via JAX-RS,
 * em vez de deixar como recurso estatico do webapp: como a Application esta
 * mapeada na raiz ("/"), um recurso estatico correria risco de brigar com o
 * roteamento do JAX-RS. Um @Path literal como este sempre tem prioridade sobre
 * o {codigo} do RedirecionadorResource, entao nao ha ambiguidade.
 */
@Path("/painel")
public class PainelResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response paginaInicial() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("estatico/index.html")) {
            if (in == null) {
                return Response.serverError().build();
            }
            String html = lerConteudo(in);
            return Response.ok(html).build();
        } catch (IOException e) {
            return Response.serverError().build();
        }
    }

    private String lerConteudo(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
