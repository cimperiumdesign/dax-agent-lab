# DAX Stream Android — atualização automática do conteúdo

Aplicativo Android que carrega o DAX Stream publicado em **https://dax-stream-dan.floot.app/** dentro de um WebView próprio (sem aba do Chrome). Quando a plataforma online é atualizada e publicada, o aplicativo carrega essas mudanças da próxima vez que abrir/recarregar.

Atenção: não é um sistema de atualização automática do próprio APK nem substitui automaticamente o APK Floot anterior (ID de pacote diferente). Modificações no código nativo continuam exigindo novo APK. A reprodução e o fullscreen de iframes externos continuam sujeitos ao comportamento/permissões do provedor do vídeo.

Para construir: execute **./gradlew assembleDebug** em um ambiente com Android SDK e Gradle. No GitHub Actions o fluxo automatizado está em .github/workflows/build-dax-stream-android.yml.
