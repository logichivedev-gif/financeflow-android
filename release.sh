#!/usr/bin/env bash

set -e

PROJECT_DIR="/home/abel/Documentos/financeflowfinal"
PROP_FILE="$PROJECT_DIR/app/version.properties"
NOTES_FILE="$PROJECT_DIR/RELEASE_NOTES.md"

cd "$PROJECT_DIR"

if [ ! -f "$PROP_FILE" ]; then
    echo "❌ Error: No se encuentra app/version.properties"
    exit 1
fi

# Extraer número de versión
MAJOR=$(grep "VERSION_MAJOR" "$PROP_FILE" | cut -d'=' -f2 | tr -d ' \r')
MINOR=$(grep "VERSION_MINOR" "$PROP_FILE" | cut -d'=' -f2 | tr -d ' \r')
PATCH=$(grep "VERSION_PATCH" "$PROP_FILE" | cut -d'=' -f2 | tr -d ' \r')

VERSION="v${MAJOR}.${MINOR}.${PATCH}"
APK_NAME="FinanceFlow-${VERSION}.apk"

echo "🚀 Preparando publicación para la versión: $VERSION"

# Configurar credenciales de Git
git config user.email "logichive.dev@gmail.com"
git config user.name "logichivedev-gif"

# Guardar cambios en Git
echo "📦 Subiendo cambios al repositorio..."
git add .
git commit -m "Release ${VERSION}: Código fuente y build actualizado" || echo "Sin cambios pendientes para commit."
git push origin main

# Verificar ubicación de la APK
if [ -f "./release/app-release.apk" ]; then
    echo "📱 Renombrando APK a ${APK_NAME}..."
    cp ./release/app-release.apk "./release/${APK_NAME}"
    APK_PATH="./release/${APK_NAME}"
elif [ -f "./release/${APK_NAME}" ]; then
    APK_PATH="./release/${APK_NAME}"
else
    echo "❌ Error: No se encontró la APK en ./release/app-release.apk ni ./release/${APK_NAME}"
    echo "Genera la APK firmada desde Android Studio antes de ejecutar."
    exit 1
fi

# Determinar qué descripción usar
if [ -f "$NOTES_FILE" ]; then
    echo "📄 Usando la descripción detallada de RELEASE_NOTES.md..."
    RELEASE_NOTES=$(cat "$NOTES_FILE")
else
    echo "ℹ️ No se encontró RELEASE_NOTES.md. Usando descripción genérica..."
    RELEASE_NOTES="Llega la versión **${VERSION}** de **FinanceFlow**. Revisa los cambios incluidos en esta actualización."
fi

# Publicar o actualizar en GitHub
echo "🌐 Publicando Release en GitHub..."
if gh release view "$VERSION" >/dev/null 2>&1; then
    echo "🔄 El release $VERSION ya existe. Actualizando archivo y descripción..."
    gh release upload "$VERSION" "$APK_PATH" --clobber
    gh release edit "$VERSION" --notes "$RELEASE_NOTES"
else
    gh release create "$VERSION" "$APK_PATH" \
      --title "FinanceFlow ${VERSION}" \
      --notes "$RELEASE_NOTES"
fi

echo "✅ ¡Publicación de $VERSION completada con éxito!"
