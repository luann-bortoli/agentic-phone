# AgentPhone

## 👥 Integrantes da Dupla
* [Luann de Bortoli]
* [Pedro dos Santos]

---

## 📝 Descrição da Proposta
O **AgentPhone** é uma aplicação Android nativa que simula uma interface de aplicativo de mensageria (estilo WhatsApp), na qual o usuário interage com **Agentes de Inteligência Artificial** personalizados.

Cada contato cadastrado possui um nome, número de telefone e uma função/especialidade (por exemplo: *Dave - Lawyer*). As conversas com cada agente são alimentadas por um modelo de linguagem (LLM) executado localmente via **Ollama**, com suporte a respostas transmitidas em tempo real (streaming).

Os contatos cadastrados no aplicativo são armazenados localmente utilizando um banco de dados **SQLite**, garantindo que as informações de contatos permaneçam salvas entre as sessões do aplicativo.

---
<img width="266" height="598" alt="image" src="https://github.com/user-attachments/assets/3dabc1b6-e08a-49ff-9575-fd59b10e43f1" />
<img width="266" height="597" alt="image" src="https://github.com/user-attachments/assets/e5e301f0-45e8-4357-80a2-392e20d57bb3" />
<img width="266" height="593" alt="image" src="https://github.com/user-attachments/assets/67a8449d-7bd1-4cb9-8b04-d778604ab5d4" />
<img width="266" height="598" alt="image" src="https://github.com/user-attachments/assets/feb8ee41-3bb8-4222-8e30-042073153cb4" />


## 🛠️ Tecnologias Utilizadas
* **Linguagem de Programação:** Kotlin
* **Interface de Usuário (UI):** Jetpack Compose com componentes Material 3
* **Arquitetura de Software:** MVVM (Model-View-ViewModel) utilizando `StateFlow` e Corrotinas
* **Banco de Dados:** SQLite nativo (`SQLiteOpenHelper`)
* **Requisições HTTP / Streaming:** Ktor Client (Engine CIO, HttpTimeout, ContentNegotiation)
* **Serialização de Dados:** `kotlinx.serialization` (JSON)
* **Navegação:** Jetpack Navigation Compose
* **Provedor de IA Local:** Ollama REST API (`/api/chat` com streaming)

---

## 🚀 Instruções de Execução

### 1. Pré-requisitos
* **Android Studio** instalado (versão com suporte a Jetpack Compose).
* **Ollama** instalado na máquina para servir o modelo LLM.
* Modelo LLM baixado no Ollama (exemplo: `qwen3.8:27b`, `llama3`, `gemma`, etc.).

### 2. Configurar e Iniciar o Servidor Ollama
Para permitir que o emulador ou dispositivo Android acesse o servidor Ollama rodando no computador host:

1. No terminal do seu computador, inicie o Ollama configurando a variável `OLLAMA_HOST` para escutar em `0.0.0.0`:
   * **Windows (PowerShell):**
     ```powershell
     $env:OLLAMA_HOST="0.0.0.0"
     ollama serve
     ```
   * **Linux / macOS:**
     ```bash
     OLLAMA_HOST=0.0.0.0 ollama serve
     ```

2. Em outro terminal, garanta que o modelo escolhido está disponível:
   ```bash
   ollama run qwen3.8:27b
   ```

### 3. Compilação e Execução do Aplicativo
1. Abra a pasta do projeto no **Android Studio**.
2. Aguarde a sincronização dos arquivos do Gradle (**Gradle Sync**).
3. Selecione o emulador Android ou dispositivo físico e clique em **Run 'app'** (`Shift + F10`).

### 4. Configurações de Conexão no App
* **Emulador Android:** A URL padrão utilizada pelo app é `http://10.0.2.2:11434` (endereço IP reservado pelo emulador Android para acessar o `localhost` da máquina host).
* **Dispositivo Físico:** Se estiver utilizando um celular conectado via USB ou Wi-Fi, abra o menu de configurações (ícone de engrenagem no canto superior direito do app) e informe o IP local da sua máquina (ex: `http://192.168.x.x:11434`).
