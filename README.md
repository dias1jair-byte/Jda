# JDA Vistorias — projeto Android

Este projeto empacota a versão atual do JDA Vistorias em um aplicativo Android nativo com uma tela WebView interna. O usuário abre pelo ícone **JDA Vistorias**; não precisa abrir o Chrome e as telas/cadastros continuam funcionando offline.

## Recursos integrados
- HTML do JDA Vistorias incluído dentro do APK (`app/src/main/assets/index.html`).
- Armazenamento local do WebView habilitado para manter os cadastros no aparelho.
- Seletor nativo de arquivos para fotos, importação e seleção de PDF.
- Compartilhamento Android nativo para PDF selecionado e backup JSON.
- Impressão nativa do relatório, permitindo escolher “Salvar como PDF”.
- Mantidos o botão **NOVO**, histórico, sequência de ambientes, finalizar ambiente e finalizar vistoria.

## Gerar o APK

O ambiente em que este projeto foi preparado não possui Android SDK nem Gradle instalado, portanto o APK ainda não foi compilado/testado aqui. Para gerar:

1. Instale o Android Studio em um computador.
2. Extraia o ZIP e abra a pasta `JDA-Vistorias-Android` no Android Studio.
3. Aguarde a sincronização do Gradle e a instalação do Android SDK 35, se solicitado.
4. Use **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. O APK de teste ficará em `app/build/outputs/apk/debug/app-debug.apk`.
6. Envie esse APK ao Redmi Note 14 5G e autorize a instalação de apps dessa origem quando o Android solicitar.

## Observações
- O primeiro build requer internet para o Android Studio baixar Gradle/Android Gradle Plugin e dependências.
- Os dados ficam no armazenamento local do aplicativo. Exporte o backup antes de desinstalar ou limpar os dados do app.
- A impressão abre o serviço de impressão do Android; escolha “Salvar como PDF”. Depois use WhatsApp no botão de compartilhamento para enviar o PDF selecionado.
