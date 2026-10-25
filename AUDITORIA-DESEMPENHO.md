# Auditoria de desempenho e apps opcionais

Data: 2026-10-25  
Dispositivo informado: Xiaomi MiTV Stick 4K (IP local omitido)  
Versão de referência: commit `78db7df2d2f7302a8e67181276f8cb6ca83878d4`

## Limite da inspeção

ADB e Android SDK foram instalados nesta máquina. A conexão de leitura identificou o aparelho como `MiTV-AYFR0`/`soul`, Xiaomi, Android 14 (API 34), ABI `armeabi-v7a`. O `/proc/meminfo` informou 1.959.796 kB totais e 593.756 kB disponíveis; `/data` mostrou 4,8 GB, 2,8 GB usados (61%). São amostras pontuais, não uma comparação controlada entre launchers.

## Medidores adicionados

No drawer inferior, ao lado do cartão de clima, há um cartão **Desempenho** inspirado na referência enviada. Abaixo do título, ele mostra a resolução física atual do display abreviada (por exemplo, 4K, 2K ou 1080p), em texto menor e discreto. A resolução e a frequência de atualização são consultadas ao abrir o app e a cada segundo enquanto o painel fica aberto, para refletir mudanças nos ajustes da TV. A metade esquerda mostra a cadência medida dos callbacks de quadro da interface como FPS e informa separadamente a frequência máxima do display em Hz. Esse FPS acompanha atrasos no loop de quadros do launcher; não é uma medição do pipeline de apresentação da GPU. A metade direita lista CPU, RAM e DISCO, com percentuais em texto e sem barras. O painel acompanha os temas claro e escuro.

- **RAM**: memória usada pelo sistema, calculada a partir de `ActivityManager.MemoryInfo`.
- **CPU**: usa a ocupação geral por `/proc/stat` se o app puder ler esse arquivo. Neste stick, essa leitura é restrita; para atender ao layout pedido, o rótulo continua **CPU**, mas o valor mede o processo Free My TV normalizado pelos processadores disponíveis.
- **DISCO**: espaço usado na partição de dados (`/data`), via `StatFs`.

As leituras ocorrem a cada segundo em uma thread de fundo, somente enquanto o drawer/painel está visível. Se uma leitura não estiver disponível, o campo correspondente mostra `--%`. As métricas não são enviadas pela rede. Após instalar o cartão novo, a captura do drawer mostrou CPU 1%, RAM 62%, DISCO 61% e frequência de tela de 60 Hz/FPS máximo. A captura anterior à mudança deste layout mostrara CPU 0%, RAM 58%, DISCO 61%, com PSS de `io.freemytv:home` em 108.265 kB. Esses valores variam com o instante e não há medição equivalente do launcher original.

## O que o projeto já otimiza

O guia `CLAUDE.md` descreve uma tela Android desenhada em Canvas, com camadas para redesenhar apenas o que mudou, animação a 60 fps somente durante movimento e atualização em baixa frequência quando parada. Há também um caminho de compatibilidade para Android 8–11 e limite de apps na prateleira em aparelhos com pouca memória. O README relata 96 MB para a tela Free My TV e 242 MB para outra tela inicial, medidos em uma TCL Google TV Android 14; esse resultado não representa uma medição deste Xiaomi.

Para este stick, a comparação correta depende de medições no próprio aparelho, usando **Home Settings → Home screen memory** e **Free memory**, tanto no modo Original quanto no Free My TV, nas mesmas condições e após estabilizar a tela. Os novos indicadores mostram ocupação geral, mas não substituem essa comparação de memória por processo.

## Apps opcionais listados no código

O array `HomeMode.DEBLOAT` do commit acima já contém estes candidatos Xiaomi. O projeto tenta apenas desativá-los no modo Free My TV, caso existam; **Original** reativa os apps que o próprio Free My TV desativou. Não é desinstalação.

| Pacote | Descrição no projeto | Escolha |
|---|---|---|
| `com.miui.tv.analytics` | Telemetria Xiaomi | ☒ desativar ☐ manter |
| `com.xiaomi.statistic` | Estatísticas/telemetria Xiaomi | ☒ desativar ☐ manter |
| `com.mitv.tvhome.mitvplus` | Canais promocionais Mi TV+ | ☒ desativar ☐ manter |
| `com.xm.webcontent` | Conteúdo web promocional | ☒ desativar ☐ manter |
| `com.google.android.play.games` | Play Games | ☒ desativar ☐ manter |
| `com.google.android.youtube.tvmusic` | YouTube Music | ☒ desativar ☐ manter |

As duas últimas entradas são candidatos genéricos do projeto, não específicos da Xiaomi. Todos os seis estão instalados neste stick. Na leitura de 2026-10-25, os seis já estavam desativados; também estavam desativados o launcher original `com.google.android.tvlauncher` e o launcher Xiaomi `com.mitv.tvhome.atv`. Não desativei nem reativei pacotes nesta tarefa. O HOME ativo já era `io.freemytv` (versão 1.2), portanto esses estados antecedem qualquer build ou instalação feita aqui.

## Componentes que o projeto protege

`HomeMode.PROTECTED` inclui serviços Xiaomi para controle do sistema, pareamento do controle remoto, TV ao vivo, ajustes rápidos, bloqueio infantil, atualização, configuração inicial, transmissão sem fio e reprodução HDMI. O guia alerta para não desligar componentes Android, instalador de pacotes, permissões, rede/Wi-Fi, entradas de TV, atualizações ou menus de fábrica. Esses componentes não são recomendados para desativação.

## Ajustes de baixo risco para considerar

- Em **Home Settings**, desligar clima ou cotações se não forem usados reduz atualizações e desenho desses widgets.
- `show_crypto=false` no `config.properties` desliga a consulta de cotações. O ganho esperado é pequeno.
- `screensaver=false` mantém o protetor de tela original em vez do Free My TV; isso evita manter a tela de descanso do app ativa, mas não deve mudar o consumo da tela inicial em uso.

`hide_animation` significa ocultar desenhos animados/anime das recomendações; não é um controle de animações da interface. Não o trate como ajuste de desempenho.

## Próxima decisão necessária

Você escolheu manter os seis desativados quando Free My TV estiver ativo. Os seis estavam desligados ao iniciar a inspeção. Ao escolher Original, quatro candidatos Xiaomi voltaram a ficar habilitados; Play Games e YouTube Music permaneceram desativados, preservando o estado anterior do sistema. Nenhum comando ADB desta tarefa desativou/reabilitou esses pacotes. Ao ativar Free My TV, o código `DEBLOAT` tentará desligar os seis; os dois que já estavam desligados continuarão assim.

## Especificações públicas do modelo

A ficha oficial Xiaomi do TV Stick 4K informa Android TV 11, CPU Cortex-A35 quad-core, 2 GB de RAM e 8 GB de armazenamento. Isso é compatível com o caminho de baixo uso de memória que o Free My TV já possui para Android 8–11, mas não confirma a versão ou os recursos livres do aparelho específico; veja a [ficha técnica oficial](https://www.mi.com/us/product/xiaomi-tv-stick-4k/specs/).


## Atualização e rollback

O APK criado localmente (`FreeMyTV.apk`, versão 1.2 / versionCode 3) compila e está assinado com a chave de desenvolvimento local `freemytv.keystore`. O app 1.2 instalado no stick tem uma assinatura diferente (certificado `CN=Free My TV, O=freemytv.io`). Por isso, `adb install -r` não pode atualizá-lo.

O APK instalado foi copiado para `FreeMyTV-before-update.apk` (o `.gitignore` ignora os APKs). Com autorização do usuário, foi selecionado Original na TV, confirmado `com.google.android.tvlauncher/.MainActivity` como HOME, removida a build oficial e instalado `FreeMyTV.apk`. A desinstalação apagou preferências/dados locais, como aprovado. A cópia permite restaurar o código assinado oficialmente, mas não os dados apagados. A nova versão está instalada e ativa como HOME. O log não mostrou falha da tela inicial. A instalação corretiva foi aplicada pela mesma chave, sem apagar os dados da nova build. Os seis pacotes escolhidos continuam desativados.

Para rollback: selecionar **Original** na tela Free My TV, desinstalar a build local e reinstalar `FreeMyTV-before-update.apk`. Isso restaura o APK oficial, mas dados removidos na reinstalação não voltam. A chave `freemytv.keystore` deve ser mantida para futuras atualizações da build própria.




