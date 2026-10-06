# Trabalho 5 — Pipeline CI Mínimo e Reprodutível

**Disciplina:** Cloud Computing e DevOps Avançado

**Curso:** CST em Sistemas para Internet (6º Período) — UniALFA

**Professor:** Ranghetti

**Valor:** Até 1,0 ponto

**Data limite de entrega:** Aula 10

## 👥 Equipe

* **Arthur Miranda Rodrigues**

* **Alexandre**

* **Heitor**


## 📌 1. Escopo e Objetivos do Trabalho

O objetivo deste trabalho é implementar e documentar um pipeline de **Integração Contínua (CI)** mínimo, reproduzível e automatizado a partir do repositório da aplicação web **Biblioteca** (Java com Spring Boot e Maven).

### Requisitos Mínimos (Obrigatórios)

* \[x\] **Repositório Acessível:** Código hospedado e acessível para avaliação do professor.

* \[x\] **Gatilho e Checkout Automatizados:** Início automático por eventos de `push` ou `pull_request` em branch principal.

* \[x\] **Build Automatizado:** Compilação da aplicação a partir de ambiente limpo e isolado (*runner* ubuntu).

* \[x\] **Testes Automatizados:** Execução automática da suíte de testes unitários existentes.

* \[x\] **Tratamento de Falhas (Fail-Fast):** Interrupção imediata do fluxo caso o build ou os testes falhem.

* \[x\] **Geração e Rastreabilidade de Artefato:** Produção de um arquivo imutável (JAR) associado à hash única do commit (`github.sha`).

* \[x\] **Evidências de Execução:** Capturas e registros comprovando uma execução com **Sucesso** e uma com **Falha**.

* \[x\] **Segurança e Reprodutibilidade:** Isenção de segredos/senhas expostos em código e garantia de execução determinística.

### Recursos Recomendados e Adicionais

* \[x\] Cache de dependências Maven (`actions/setup-java` com `cache: maven`).

* \[x\] Publicação do artefato compilado nos artefatos do workflow (`upload-artifact`).

* \[ \] Construção e publicação de imagem Docker no container registry (Escopo futuro / CD).

## 🏛️ 2. Arquitetura do Projeto

A aplicação segue uma arquitetura em camadas (`MVC` + `Service` + `Repository`):

* `com.unialfa.biblioteca.controller`: Controllers MVC (ThymeLeaf).

* `com.unialfa.biblioteca.api`: Endpoints REST protegidos via JWT (`Bearer Token`).

* `com.unialfa.biblioteca.service`: Regras de negócio e serviços transversais.

* `com.unialfa.biblioteca.repository`: Repositórios Spring Data JPA.

* `com.unialfa.biblioteca.model`: Entidades de domínio JPA (`Usuario`, `Livro`, `Autor`).

* `com.unialfa.biblioteca.config`: Configurações de segurança e autorização (`SecurityConfiguration`).

## ⚙️ 3. Estrutura do Pipeline CI (GitHub Actions)

O pipeline foi configurado no arquivo `.github/workflows/ci.yml` e mapeia a seguinte esteira de execução:

```
[ Gatilho: Push / PR ] 
          │
          ▼
   1. Checkout Code
          │
          ▼
   2. Setup JDK 17 & Cache
          │
          ▼
   3. Grant Executable Permissions (mvnw)
          │
          ▼
   4. Build & Run Tests (mvn clean test)
          │
          ▼
   5. Package Application (mvn package)
          │
          ▼
   6. Upload Immutable Artifact (JAR + Commit SHA)

```

### Detalhamento das Etapas do Pipeline

|

| **Etapa** | **Entrada** | **Ação Executada** | **Saída / Artefato** | **Critério de Falha** | **Responsável** |
| **Gatilho & Checkout** | Evento `push` / `pull_request` | Clone do código fonte | Código na versão exata do commit | Falha de autenticação/repositório | GitHub Actions Runner |
| **Setup Ambiente** | Ubuntu Latest | Instalação do Temurin JDK 17 e Cache Maven | JVM pronta e dependências baixadas | Versão de Java incompatível ou indisponibilidade do runner | GitHub Actions Runner |
| **Build & Testes** | Código fonte | Execução do comando `./mvnw clean test` | Logs de teste e relatórios Surefire | Erro de compilação ou 1+ teste falho | Equipe de Dev / QA |
| **Empacotamento** | Código testado | Execução do comando `./mvnw package -DskipTests` | Arquivo `.jar` em `target/` | Falha na criação do pacote | Equipe de Dev |
| **Publicação Artefato** | Arquivo `target/*.jar` | Upload via `actions/upload-artifact@v4` | Artefato baixável na aba Actions | Falha na transferência do arquivo | Equipe de DevOps |

## 📋 4. Critérios de Avaliação e Checklist do T5

| **Critério Avaliado** | **Status** | **Descrição de Atendimento** |
| **Pipeline executável e reprodutível** | ✅ | Workflow em `.github/workflows/ci.yml` capaz de rodar autonomamente em ambiente limpo. |
| **Build, testes e tratamento de falhas** | ✅ | Utiliza o wrapper `./mvnw`, executa testes e interrompe o job imediatamente com código de saída `!= 0`. |
| **Artefato e rastreabilidade** | ✅ | Artefato nomeado dinamicamente com o ID único do commit (`biblioteca-app-${{ github.sha }}`). |
| **Evidências e instruções** | ✅ | Roteiro estruturado neste README e capturas armazenadas em `docs/evidencias/`. |
| **Segurança básica e organização** | ✅ | Credenciais e propriedades sensíveis isoladas do código; uso de variáveis e Secrets. |

## 🧪 5. Instruções para Investigação e Simulação de Falhas

Para validar os critérios exigidos pelo professor no T5, siga os procedimentos abaixo:

### A. Execução com Sucesso (Sucesso / Verde)

1. Faça qualquer alteração válida de código ou documentação na branch `main`.

2. Envie o commit via `git push origin main`.

3. Acesse a aba **Actions** no repositório GitHub e observe a execução do workflow **CI Pipeline - T5** finalizando com o ícone verde `Success`.

4. Faça o download do artefato gerado no rodapé da página da execução.

### B. Execução com Falha Detectada (Falha / Vermelho)

1. Crie uma branch de teste:

   ```
   git checkout -b teste/simulacao-falha
   
   ```

2. Abra um arquivo de teste em `src/test/java/` e adicione ou altere uma validação para forçar uma falha. Exemplo:

   ```
   Assertions.assertEquals(1, 2, "Simulação de falha no pipeline CI");
   
   ```

3. Envie o commit e abra um Pull Request para a `main`:

   ```
   git add .
   git commit -m "test: forca erro para evidencia do T5"
   git push origin teste/simulacao-falha
   
   ```

4. Verifique a aba **Actions**: a pipeline irá falhar na etapa **Build e Testes com Maven**.

5. Obtenha o print do status em vermelho e do log detalhado da falha do teste para salvar na pasta `docs/evidencias/`.

## 🔒 6. Gestão de Segredos e Práticas de Segurança

* **Segurança de Código:** Nenhuma senha, token JWT ou chave privada está fixada nos arquivos do repositório.

* **Configuração de Variáveis Sensíveis:** Caso a aplicação necessite de credenciais para ambientes externos, as variáveis devem ser cadastradas em **Settings > Secrets and variables > Actions** do repositório no GitHub e injetadas no arquivo `ci.yml` utilizando a sintaxe `${{ secrets.NOME_DO_SEGREDO }}`.

## 🚀 7. Como Executar o Projeto Localmente

### Pré-requisitos

* **Java JDK 17** instalado e configurado nas variáveis de ambiente.

### Passos

1. Clonar o repositório:

   ```
   git clone https://github.com/Arthur-Miranda-Rodrigues/Trabalho-Cloud-Computing-e-DevOps-Avançado-T5.git
   cd Trabalho-Cloud-Computing-e-Devops-Avancado-T5

   
   ```

2. Compilar e executar os testes unitários:

   ```
   ./mvnw clean test
   
   ```

3. Gerar o artefato compilado (JAR):

   ```
   ./mvnw package -DskipTests
   
   ```

4. Iniciar a aplicação:

   ```
   java -jar target/biblioteca-0.0.1-SNAPSHOT.jar
   
   ```