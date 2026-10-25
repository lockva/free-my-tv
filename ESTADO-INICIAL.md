# Estado inicial do Free My TV

Data do registro: 2026-10-25

## Cópia local

- Pasta: diretório local do projeto
- Repositório remoto: `https://github.com/simbasjungles/free-my-tv.git`
- Branch inicial: `main`, acompanhando `origin/main`
- Commit inicial: `78db7df2d2f7302a8e67181276f8cb6ca83878d4`
- Data do commit: `2026-10-25 14:18:54 -0300`
- Mensagem: `README: the Xiaomi Mi TV Stick among the streamers (#279, reported working on Android 9)`
- Estado no clone: limpo antes deste registro; nenhuma alteração de código local preexistente.
- Dispositivo informado: Xiaomi MiTV Stick 4K (IP local omitido; não conectado nem inspecionado).

## Estrutura relevante para a identidade visual

- `app/src/io/freemytv/HomeView.java`: tela inicial Android, desenhada em Canvas.
- `app/src/io/freemytv/HomeActivity.java`: atividade que hospeda a tela inicial.
- `app/src/io/freemytv/Backdrop.java`, `Frost.java`, `Shaders.java`, `Glyphs.java`, `Symbols.java` e `Ui.java`: componentes visuais/recursos de desenho.
- `app/assets/` e `app/res/`: fontes, ícones e recursos Android.
- `web/`: implementação web para TVs LG/Samsung; não é a tela usada pelo Android MiTV Stick.
- `build.sh`: gera o APK Android (`FreeMyTV.apk`); projeto Android sem Gradle, compilado com Android SDK.

## Instalação descrita pelo projeto (Android TV)

O README e `CLAUDE.md` documentam o build Android com JDK 17+, Android SDK (build-tools 34+, platform android-34) e `adb`:

1. Ativar opções de desenvolvedor e depuração USB na TV. O usuário confirma o diálogo de depuração na própria TV.
2. Executar `./build.sh` na raiz do projeto para compilar `FreeMyTV.apk`.
3. Conectar e instalar a atualização:

   ```bash
   adb connect <IP-DA-TV>:5555
   adb install -r FreeMyTV.apk
   ```

4. Abrir Free My TV na TV e selecionar **Home Screen → Free My TV** para trocar a tela inicial. Essa etapa altera o modo da TV e deve ser confirmada pelo usuário no próprio dispositivo.

No Windows, o projeto recomenda Git Bash ou WSL para `build.sh`. Manter `freemytv.keystore` é necessário para futuras atualizações assinadas com a mesma chave. A versão do Android não foi verificada no stick. A ficha oficial do Xiaomi TV Stick 4K informa Android TV 11; confirme no próprio aparelho, pois variantes e atualizações podem divergir.

## Reversão

- Na TV: **Free My TV → Home Screen → Original** restaura a tela inicial original e desfaz a troca de modo; é a rota de retorno indicada pelo projeto.
- Reverter apenas uma alteração local de código: revisar `git diff` e restaurar os arquivos alterados antes do build; não apagar a chave de assinatura nem instalar um APK assinado por chave incompatível por cima de outro.
- A instalação documentada usa `adb install -r`, que atualiza o app sem remover seus dados. Não executei comandos ADB nem alterei o dispositivo.

## Limites deste registro

Este é o ponto de partida anterior a mudanças de identidade visual. O commit local é a referência de código original; este arquivo é o único arquivo novo e registra esse estado. Nenhum APK foi compilado ou instalado.

