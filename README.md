[![coverage](https://raw.githubusercontent.com/luizpaulino/streaming/badges/jacoco.svg)](https://github.com/luizpaulino/streaming/actions/workflows/build.yml)
## Setup e uso da aplicação

- Requisitos:
    - Docker
    - Maven

- Clonar o projeto e rodar o script flix.sh
- Collection do [arquivo JSON da coleção](./streaming_collection.json).
- Para assistir o vídeo, basta acessar o endpoint raiz da aplicação. exemplo: http://localhost:8080 e adicionar o id do vídeo no campo Video ID.

### Endpoints

- **Streaming**
  - Adiciona informações do vídeo: GET - /streaming/{idVideo}

    **Tipo de Requisição:** GET

    **Exemplo de Requisição:**
    - **Header:**
      - Range: bytes=0-500
      - Cookie: 10

    **Código de Resposta:** 206 Partial Content
      ```
      O conteúdo da resposta é o streaming do vídeo.
      ```
