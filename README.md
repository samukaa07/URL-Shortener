# URL Shortener

Desafio técnico da Topaz: um encurtador de URLs simples, rodando em WildFly 10 com Java 8, JAX-RS, CDI e JPA/Hibernate.

## Objetivo

Receber uma URL e devolver uma versão curta dela. Quem acessar a URL curta é redirecionado (HTTP 302) para a URL
original. O usuário pode escolher um "apelido" (alias) próprio para a URL curta, ou deixar o sistema gerar um
código automaticamente.

## Tecnologias

- Java 8
- JAX-RS (RESTEasy, embutido no WildFly)
- CDI
- EJB (Stateless, só pra aproveitar o container de transação/pool que o WildFly já oferece)
- JPA / Hibernate
- H2 (datasource `ExampleDS` que já vem configurado por padrão no WildFly 10)
- Maven
- JUnit 5 + Mockito (testes)

## Arquitetura

Camadas bem separadas, fluxo simples de cima pra baixo:

```
Resource (JAX-RS)
      ↓
Service (regra de negócio)
      ↓
Repository (JPA / EntityManager)
      ↓
Banco (H2)
```

O Resource só recebe a requisição HTTP e devolve a resposta - não tem regra de negócio nele. Toda a validação,
geração de código e decisão sobre alias fica no `EncurtadorService`. O `UrlRepositorio` só sabe salvar e consultar.

## Estrutura do projeto

```
src/main/java/br/com/urlshortener
├── config
│   ├── JaxRsAplicacao.java        (liga o JAX-RS na aplicação)
│   └── ConfiguracaoApp.java       (lê a URL base do arquivo de properties)
├── resource
│   ├── UrlResource.java           (POST /api/urls)
│   ├── RedirecionadorResource.java (GET /{codigo})
│   └── PainelResource.java        (GET /painel - front-end)
├── service
│   └── EncurtadorService.java     (regra de negócio + geração sincronizada)
├── repository
│   └── UrlRepositorio.java        (EntityManager)
├── entity
│   └── UrlCurta.java
├── dto
│   ├── CriarUrlRequisicao.java
│   ├── UrlRespostaDto.java
│   └── ErroRespostaDto.java
├── exception
│   ├── ErroDeNegocioException.java (base, carrega o status HTTP)
│   ├── UrlInvalidaException.java
│   ├── AliasInvalidoException.java
│   ├── AliasEmUsoException.java
│   ├── CodigoNaoEncontradoException.java
│   ├── ManipuladorErroNegocio.java (ExceptionMapper -> JSON padronizado)
│   └── ManipuladorErroGenerico.java (pega qualquer erro não mapeado -> 500)
└── util
    ├── GeradorDeCodigo.java
    └── ValidadorUtil.java
```

## Como executar

Pré-requisitos: JDK 8, Maven e um WildFly 10 instalado.

1. Gerar o WAR:
   ```
   mvn clean package
   ```
2. Copiar `target/url-shortener.war` para a pasta `standalone/deployments` do WildFly 10.
3. Subir o servidor (`standalone.bat` ou `standalone.sh`).
4. A aplicação sobe em `http://localhost:8080/url-shortener`.

Não precisa configurar nenhum banco à parte: o `persistence.xml` aponta pro datasource `ExampleDS`, que é um H2
em memória que já vem pronto de fábrica no WildFly 10.

5. Front-end: abrir `http://localhost:8080/url-shortener/painel` no navegador.

## Front-end

Uma página HTML/CSS/JS simples (sem framework) em `GET /painel`. Ela deixa digitar a URL,
um alias opcional, chama o `POST /api/urls` via `fetch` e mostra a URL encurtada com um botão de copiar.

Ela é servida por um resource JAX-RS (`PainelResource`) que lê o HTML do classpath e devolve como `text/html`, em
vez de ficar como arquivo estático dentro do `webapp`. O motivo: a `Application` do JAX-RS está mapeada na raiz
do contexto (`@ApplicationPath("/")`, necessário pra atender o requisito de `GET /{codigo}` sem prefixo), e isso
faz o RESTEasy competir pelo roteamento de toda URL do contexto - inclusive as que normalmente seriam resolvidas
como arquivo estático. Um `@Path` literal (`/painel`) sempre é mais específico que o `{codigo}` do
`RedirecionadorResource`, então não tem ambiguidade nenhuma nem risco de um "engolir" o outro.

## API

### POST /api/urls

Cria uma URL curta.

Request (sem alias):
```json
{ "url": "https://www.google.com" }
```

Request (com alias):
```json
{ "url": "https://www.google.com", "alias": "google" }
```

Response (201 Created):
```json
{ "shortUrl": "http://localhost:8080/url-shortener/google" }
```

### GET /{codigo}

Redireciona para a URL original.

- Encontrou: `302 Found` com header `Location` apontando pra URL original.
- Não encontrou: `404 Not Found`.

## Tratamento de erros

Todo erro de negócio devolve um JSON no formato:
```json
{ "status": 409, "mensagem": "Alias 'google' ja esta em uso." }
```

| Situação                              | Status |
|----------------------------------------|--------|
| URL vazia ou em formato inválido       | 400    |
| Alias com caracteres não permitidos    | 400    |
| Alias já em uso                        | 409    |
| Código/alias não encontrado no GET     | 404    |
| Qualquer erro inesperado               | 500    |
| URL criada com sucesso                 | 201    |
| Redirecionamento                       | 302    |

Regra de alias: só letras, números, hífen e underscore (`^[a-zA-Z0-9_-]+$`). Sem espaços.

## Decisões técnicas

- **Sem Spring Boot**: o desafio deixa claro que o ambiente real da empresa é Java EE (WildFly 10), então optei por
  usar as tecnologias nativas do próprio servidor (JAX-RS, CDI, EJB, JPA) em vez de trazer um framework que não
  seria usado de verdade lá.
- **EJB `@Stateless` no service**: dá acesso ao pool/gerenciamento de transação do container de graça, sem precisar
  configurar nada extra. Como o método de geração de código precisa ser sincronizado "manualmente" mesmo assim
  (bean stateless não serializa chamadas concorrentes), isso não conflita com o requisito.
- **`@ApplicationException` nas exceções de negócio**: por padrão, um EJB `@Stateless` embrulha qualquer
  `RuntimeException` lançada de dentro dele numa `EJBException`, tratando como erro de sistema. Isso faria toda
  exceção de negócio (`AliasEmUsoException`, `UrlInvalidaException` etc.) virar 500 em vez do status correto.
  Marcando `ErroDeNegocioException` com `@ApplicationException(rollback = true)`, o container repassa a exceção
  original sem embrulhar, e o `ManipuladorErroNegocio` consegue interceptá-la normalmente.
- **H2 via `ExampleDS`**: o WildFly 10 já sobe com esse datasource H2 em memória configurado por padrão. Usar ele
  evita ter que instalar driver, criar módulo JBoss ou depender de qualquer banco externo pra rodar o projeto.
- **DTOs separados da entidade**: `CriarUrlRequisicao`/`UrlRespostaDto` nunca expõem `UrlCurta` diretamente, então
  dá pra mudar o modelo de persistência sem afetar o contrato da API.
- **Exceções de negócio com status embutido**: em vez de um `switch` gigante decidindo o status HTTP, cada exceção
  (`AliasEmUsoException`, `CodigoNaoEncontradoException` etc.) já sabe seu próprio status. Um único
  `ExceptionMapper` (`ManipuladorErroNegocio`) resolve todas elas. Um segundo mapper (`ManipuladorErroGenerico`)
  serve de rede de segurança pra qualquer coisa inesperada, devolvendo 500 sem vazar stacktrace.
- **Geração de código de 6 caracteres**, misturando letras maiúsculas, minúsculas e números - dá mais de 56
  bilhões de combinações, suficiente pra esse escopo sem precisar de nada mais sofisticado.

## Concorrência (o motor de geração)

O requisito pede que a geração do código automático seja processada "uma de cada vez". A trava fica só no trecho
crítico, dentro de `EncurtadorService.gerarCodigoDisponivel()`:

```java
private static final Object TRAVA_GERACAO = new Object();

private String gerarCodigoDisponivel() {
    synchronized (TRAVA_GERACAO) {
        String candidato;
        do {
            candidato = geradorDeCodigo.gerar();
        } while (urlRepositorio.existePorCodigo(candidato));
        return candidato;
    }
}
```

O `synchronized` está num objeto `static`, então mesmo o container criando várias instâncias do EJB (pool de
stateless beans), todas competem pela mesma trava - garantindo que só uma thread por vez gera + confere colisão.
O resto do método (`criarUrlCurta`), incluindo a validação e o `salvar()`, roda fora da trava, então a aplicação
não fica travada como um todo, só esse pedacinho específico.

Trade-off consciente: a confirmação final de unicidade ainda depende da constraint `UNIQUE` na coluna `codigo` do
banco, como segunda camada de proteção. Numa aplicação com múltiplas instâncias/JVMs, o `synchronized` sozinho não
seria suficiente (ele só protege dentro do processo) - mas pra esse desafio, rodando uma única instância, atende
bem o que foi pedido.

## Testes

Testes unitários com JUnit 5 + Mockito, separados em duas frentes:

**Regra de negócio** (`EncurtadorServiceTest`, `GeradorDeCodigoTest`):
- criação sem alias / com alias disponível;
- rejeição de alias duplicado e de alias com caracteres inválidos;
- rejeição de URL inválida/vazia;
- resolução de código existente e erro para código inexistente;
- nova tentativa de geração quando o primeiro código sorteado colide;
- teste de concorrência disparando várias threads contra o mesmo service, validando que todos os códigos saem
  únicos;
- o gerador produz sempre códigos de 6 caracteres alfanuméricos.

**Camada de resource / integração dos componentes** (`UrlResourceTest`, `RedirecionadorResourceTest`,
`ManipuladorErroNegocioTest`):
- `POST /api/urls` devolve 201 com o `shortUrl` que o service retornou;
- `GET /{codigo}` devolve 302 com o header `Location` certo;
- uma `CodigoNaoEncontradoException` lançada pelo service não é engolida pelo resource;
- cada exceção de negócio (`AliasEmUsoException`, `CodigoNaoEncontradoException`, `UrlInvalidaException`) vira o
  status HTTP e o JSON de erro esperados através do `ManipuladorErroNegocio`.

Não foram feitos testes subindo o WildFly de verdade (via Arquillian, por exemplo): dado o tempo do desafio, essa
infraestrutura agregaria mais complexidade de setup do que valor pra avaliar as regras de negócio, que é o foco
aqui. Os testes de resource acima já cobrem a integração entre as camadas (Resource → Service → tratamento de
erro) sem precisar de um servidor de verdade no ar.

Detalhe técnico: o `javaee-api` usado no projeto só traz as interfaces do JAX-RS (é o servidor que fornece a
implementação em tempo de execução). Como alguns testes montam um `javax.ws.rs.core.Response` fora de um servidor
rodando, foi preciso adicionar `jersey-common` em escopo `test` só pra suprir essa implementação durante os
testes - ela não entra no WAR final nem interfere no RESTEasy do WildFly.

Para rodar:
```
mvn test
```

## O que ficou de fora (de propósito)

Docker e pipeline de CI/CD são citados como opcionais no desafio. Optei por não implementar nenhum dos dois pra
manter o foco no back-end, que é o que o desafio realmente avalia (arquitetura, regra de negócio, tratamento de
erro e testes). O front-end foi implementado de forma bem simples.

## O que faria diferente com mais tempo

- Cache (Redis) pra consultas de redirecionamento muito frequentes.
- Trocar o H2 por PostgreSQL num ambiente real, com um datasource próprio.
- Expiração de URLs (TTL) e um job de limpeza.
- Contagem de cliques / analytics básico.
- Rate limiting pra evitar abuso na criação de URLs.
- Testes de carga pra validar o comportamento da geração sincronizada sob volume real.
- Pipeline de CI simples (build + testes) e Dockerfile pra facilitar o deploy.
- Se o domínio exigisse, autenticação (API key ou JWT) pra quem cria as URLs.
