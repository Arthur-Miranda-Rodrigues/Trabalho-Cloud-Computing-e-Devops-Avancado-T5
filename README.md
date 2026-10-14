# Trabalho 5 — Pipeline CI Mínimo e Reprodutível

**Disciplina:** Cloud Computing e DevOps Avançado
**Curso:** CST em Sistemas para Internet (6º Período) — UniALFA
**Professor:** Ranghetti
**Valor:** Até 1,0 ponto
**Entrega:** Aula 10

## 👥 Equipe

* **Arthur Miranda Rodrigues — RA: 14538**
* **Alexandre Aizono — RA: 240034**
* **Heitor dos Santos Corrêa — RA: 14625**

**Repositório:** https://github.com/Arthur-Miranda-Rodrigues/Trabalho-Cloud-Computing-e-Devops-Avancado-T5

## 📌 1. Escopo e Objetivos

Implementar e documentar um pipeline de **Integração Contínua (CI)** mínimo, reproduzível e automatizado para a aplicação web **Biblioteca** (Java 17, Spring Boot e Maven), usando **GitHub Actions**.

### Requisitos obrigatórios (slide 65)

* [x] **Checkout:** `actions/checkout@v4` obtém a versão exata do commit.
* [x] **Gatilho definido:** `push` e `pull_request` em `main`/`master` (e `workflow_dispatch` para execução manual).
* [x] **Build:** `./mvnw clean test` em runner limpo (`ubuntu-24.04`).
* [x] **Testes existentes:** suíte Maven executada a cada integração.
* [x] **Falha interrompe o fluxo:** qualquer etapa com erro encerra o job; o artefato não é publicado.
* [x] **Resultado visível:** status na aba *Actions*, no commit e no Pull Request.
* [x] **Artefato identificado:** JAR publicado como `biblioteca-app-<SHA do commit>`.

### Recomendados

* [x] Cache de dependências Maven (`setup-java` com `cache: maven`).
* [x] Detecção básica de segredos fixos (etapa "Verificar segredos fixos no código").
* [x] Relatórios de teste (Surefire) publicados como evidência, inclusive em falha.
* [] Análise estática de código (fora do escopo desta entrega).
* [ ] Construção/publicação de imagem Docker (escopo futuro / CD; o `Dockerfile` existe, mas não faz parte deste pipeline).

## 🏛️ 2. Arquitetura da Aplicação

Camadas `MVC` + `Service` + `Repository`:

* `controller`: controllers MVC (Thymeleaf)
* `api`: endpoints REST protegidos por JWT (Bearer Token)
* `service`: regras de negócio e geração de token
* `repository`: Spring Data JPA
* `model`: entidades `Usuario`, `Livro`, `Autor`
* `config`: segurança e autorização

## ⚙️ 3. Pipeline CI (`.github/workflows/ci.yml`)

```text
[ Gatilho: push / pull_request / manual ]
                │
                ▼
        1. Checkout do código
                │
                ▼
        2. Validação inicial: segredo fixo no código?  ──(sim)──► FALHA
                │
                ▼
        3. Setup JDK 17 + cache Maven
                │
                ▼
        4. chmod +x mvnw
                │
                ▼
        5. ./mvnw clean test  ──(erro de build ou teste)──► FALHA (job interrompido)
                │
                ▼
        6. Publicar relatórios de teste (sempre)
                │
                ▼
        7. ./mvnw package -DskipTests  → target/*.jar
                │
                ▼
        8. Publicar artefato  biblioteca-app-<SHA>
```

### Detalhamento das etapas

| Etapa | Entrada | Ação | Saída / Evidência | Critério de falha | Depende de | Responsável |
|---|---|---|---|---|---|---|
| Gatilho e checkout | Evento `push`/`pull_request` | `actions/checkout@v4` | Código na versão exata do commit | Falha de acesso ao repositório | GitHub Actions | Runner / DevOps |
| Validação inicial | Arquivos de `src/main/resources` | `grep` por segredo fixo | Log da etapa | Segredo literal encontrado | Checkout | Equipe de Dev |
| Setup do ambiente | `ubuntu-24.04` | Temurin JDK 17 e cache Maven | JVM e dependências prontas | Java indisponível | Actions, Maven Central | Runner |
| Build e testes | Código-fonte | `./mvnw --batch-mode clean test` | Log e relatórios Surefire | Erro de compilação ou 1+ teste falho | JDK, dependências Maven | Equipe de Dev / QA |
| Relatórios | `target/surefire-reports/` | `upload-artifact` (`if: always()`) | Artefato `relatorios-testes-<SHA>` | — | Build e testes | DevOps |
| Empacotamento | Código testado | `./mvnw package -DskipTests` | JAR em `target/` | Falha ao gerar o pacote | Build e testes aprovados | Equipe de Dev |
| Publicação do artefato | `target/*.jar` | `upload-artifact` | Artefato `biblioteca-app-<SHA>` | Nenhum JAR encontrado | Empacotamento | DevOps |

## 📋 4. Checklist do T5

| Critério | Status | Como é atendido |
|---|---|---|
| Pipeline executável e reprodutível | ✅ | Workflow versionado, runner e JDK fixados, `mvnw` e cache Maven |
| Build, testes e tratamento de falhas | ✅ | `clean test`; falha interrompe o job (evidência de falha abaixo) |
| Artefato e rastreabilidade | ✅ | `biblioteca-app-${{ github.sha }}` associa JAR e commit |
| Evidências e instruções | ✅ | Seções 5 a 7 e pasta `docs/evidencias/` |
| Segurança básica e organização | ✅ | Segredo JWT fora do código (`JWT_SECRET`), `permissions: contents: read`, verificação de segredo fixo no CI |

## 🧪 5. Evidências

As capturas ficam em `docs/evidencias/`. As principais:

| Evidência | Arquivos |
|---|---|
| Criação do workflow no GitHub | `evidencia 11.png` a `evidencia 13.png` |
| Execução **com sucesso** (resumo e logs das etapas) | `evidencia 14.png` a `evidencia 24.png` |
| Artefato gerado, baixado e JAR extraído | `evidencia 25.png` a `evidencia 27.png` |
| Execução local do mesmo comando (`clean test`, `package`, `java -jar`) | `evidencia 4.png` a `evidencia 10.png` |
| Execução com **falha** detectada (teste forçado em Pull Request) | `evidencia 39.png` a `evidencia 43.png` (log da falha em `evidencia 43.png`) |

As demais imagens são material de apoio. A `evidencia 41.png` mostra uma execução anterior com etapas de Docker/Kubernetes que não fazem parte do pipeline entregue; a evidência oficial de falha é a `evidencia 43.png`.

## 🔍 6. Como investigar uma falha

1. Abra a aba **Actions** e clique na execução com o ícone vermelho (ou o link do check no Pull Request).
2. Identifique a **etapa** marcada com ❌ (por exemplo, *Build e Testes com Maven*).
3. Abra o log da etapa e procure `[ERROR]` e `Tests run:` para ver o teste ou a classe com problema.
4. Se precisar de mais detalhe, baixe o artefato `relatorios-testes-<SHA>` (relatórios Surefire).
5. Corrija o código ou o teste, envie um novo commit e aguarde a nova execução.

### Simular uma falha (usado nas evidências)

```bash
git checkout -b teste/simulacao-falha
# em src/test/java/..., adicione: Assertions.assertEquals(1, 2, "Simulação de falha no pipeline CI");
git add .
git commit -m "test: forca erro para evidencia do T5"
git push origin teste/simulacao-falha
# abra um Pull Request para a main e observe a falha na etapa "Build e Testes com Maven"
```

## 🔒 7. Segredos e segurança

* O segredo JWT **não está no repositório**. A aplicação lê `JWT_SECRET` (mínimo 32 caracteres) do ambiente (`api.security.token.secret=${JWT_SECRET}`).
* Os testes usam uma chave própria de teste definida em `BibliotecaApplicationTests` (não é usada em execução).
* O CI não precisa de segredos: build, testes e empacotamento não exigem `JWT_SECRET`. Se um passo futuro precisar, cadastre em **Settings → Secrets and variables → Actions** e use `${{ secrets.NOME }}`.
* A etapa "Verificar segredos fixos no código" reprova o pipeline se `src/main/resources` tiver um segredo literal.
* O workflow usa `permissions: contents: read` (menor privilégio).
* Arquivos `.env` estão no `.gitignore`.

## 🚀 8. Como executar localmente

**Pré-requisito:** Java JDK 17.

```bash
git clone https://github.com/Arthur-Miranda-Rodrigues/Trabalho-Cloud-Computing-e-Devops-Avancado-T5.git
cd Trabalho-Cloud-Computing-e-Devops-Avancado-T5

./mvnw clean test            # compila e executa os testes
./mvnw package -DskipTests   # gera target/biblioteca-0.0.1-SNAPSHOT.jar
```

Para iniciar a aplicação, defina o segredo antes:

```bash
# Linux/macOS
export JWT_SECRET=$(openssl rand -hex 32)
java -jar target/biblioteca-0.0.1-SNAPSHOT.jar
```

```powershell
# Windows PowerShell
$env:JWT_SECRET = -join ((48..57)+(97..102) | Get-Random -Count 64 | ForEach-Object {[char]$_})
java -jar target/biblioteca-0.0.1-SNAPSHOT.jar
```

