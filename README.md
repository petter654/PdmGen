# Gerador de PDM Inteligente

## 1. Visão Geral

Esta é uma aplicação web desenvolvida para auxiliar a equipe de Dados Mestres na criação padronizada de Descrições Longas (PDM) para materiais. O objetivo principal é garantir consistência, reduzir erros e agilizar o processo de cadastro.

A ferramenta funciona lendo os padrões de atributos diretamente da nossa "Base PDM Aegea 2025" em Excel e oferecendo uma interface simples para que os analistas preencham os valores corretos para cada tipo de material. A própria interface permite atualizar a base de conhecimento da ferramenta.


## 2. Tecnologias Utilizadas

* **Backend:** Java 17, Spring Boot, Apache POI (para leitura de Excel), Jackson (para JSON).
* **Frontend:** HTML, CSS, JavaScript (com Thymeleaf para servir a página).
* **Empacotamento:** Maven.
* **Execução:** JRE embutido, iniciado via script `.bat`.

## 3. Como Executar a Ferramenta

Para iniciar a ferramenta, **não é necessário instalar nada** além do pacote que você recebeu.

1.  Certifique-se de que a estrutura de pastas está correta:
    * `GeradorPdmAPP.bat` (Arquivo para iniciar)
    * `arquivos/`
        * `gerador-pdm-0.0.1-SNAPSHOT.jar` (O nome exato do seu `.jar`)
        * `jre/` (Pasta com o Java embutido)
        * `parametros_por_tipo.json` (Gerado/Atualizado automaticamente)
        * `README.md` (Este arquivo)
2.  Dê um **duplo clique** no arquivo `GeradorPdmAPP.bat`.
3.  Uma janela de terminal preta se abrirá. **Não feche esta janela!** Ela é o servidor da aplicação.
4.  Aguarde cerca de 10 segundos. O script automaticamente abrirá a ferramenta no seu navegador padrão no endereço `http://localhost:8080`.
5.  Para parar a ferramenta, simplesmente feche a janela preta do terminal.

## 4. Como Utilizar a Ferramenta

A interface possui duas seções principais na mesma página:

### 4.1. Área de Atualização 

* **Objetivo:** Manter a base de conhecimento da ferramenta atualizada com a última versão da `Base PDM Aegea 2025.xlsx`.
* **Passos:**
    1.  Clique em "Escolher arquivo".
    2.  Selecione o arquivo `.xlsx` mais recente da Base Geral.
    3.  Clique em "Processar e Atualizar Base".
    4.  Aguarde a mensagem de status indicar sucesso ("Arquivo processado...").
    5.  A página será **recarregada automaticamente** após alguns segundos, pronta para usar os novos dados.
* **Frequência:** Realize este processo sempre que a Base Geral for atualizada.

### 4.2. Área do Analista (Uso Diário)

* **Objetivo:** Gerar a string da Descrição Longa padronizada.
* **Passos:**
    1.  No campo "Buscar Classe de Material", comece a digitar o nome do material (originado da Coluna A do Excel).
    2.  Uma lista de sugestões aparecerá. **Clique na classe exata** que você deseja ou digite o nome completo e pressione **Enter**.
    3.  Os campos de atributos correspondentes (extraídos da Coluna C do Excel) serão exibidos abaixo.
    4.  Preencha os valores para cada atributo necessário. **Deixe em branco os atributos que não se aplicam**.
    5.  Clique no botão **"Gerar Descrição Longa"**.
    6.  A string final da PDM aparecerá na caixa de texto "Descrição Longa Gerada", pronta para ser copiada (`Ctrl+C`). A string já estará formatada corretamente (ex: `CLASSE; ATRIBUTO1: VALOR1; ATRIBUTO2: VALOR2`).

## 5. Troubleshooting (Problemas Comuns)

* **A aplicação não abre no navegador / Erro "Não é possível acessar esse site":**
    * Verifique se a janela preta do terminal (servidor) está aberta e não mostrou erros críticos ao iniciar.
    * Verifique se nenhuma outra aplicação está usando a porta `8080` no seu computador.
    * Aguarde um pouco mais após iniciar o `.bat`; a aplicação pode demorar alguns segundos para "subir".
* **Busca não retorna a classe desejada / Campos não aparecem após clicar na sugestão:**
    * **Principal Causa:** A base de dados pode não ter sido atualizada corretamente. Use a "Área de Atualização" para carregar o Excel mais recente.
    * Verifique se o nome da classe na Coluna A do Excel corresponde ao que você está buscando.
    * Verifique se a Coluna C do Excel para aquela classe contém os atributos no formato `NOME_DO_ATRIBUTO:`.
* **Outros Erros:** Entre em contato com Petter Dellis ou Júlia Neris (Dados Mestres).

---

**Desenvolvido por:** Petter Dellis & Júlia Neris (Dados Mestres)
**Data:** 20/10/2025
