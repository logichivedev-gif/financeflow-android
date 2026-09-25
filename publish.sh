#!/usr/bin/env bash

set -e

# Detectar automáticamente la ruta actual en Nobara Linux
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROP_FILE="$PROJECT_DIR/app/version.properties"
NOTES_FILE="$PROJECT_DIR/RELEASE_NOTES.md"

cd "$PROJECT_DIR" || exit 1

if [ ! -f "$PROP_FILE" ]; then
    echo "❌ Error: No existe app/version.properties"
    read -p "Presiona Enter para cerrar..."
    exit 1
fi

MAJOR=$(grep "^VERSION_MAJOR" "$PROP_FILE" | cut -d"=" -f2 | tr -d " \r\t")
MINOR=$(grep "^VERSION_MINOR" "$PROP_FILE" | cut -d"=" -f2 | tr -d " \r\t")
PATCH=$(grep "^VERSION_PATCH" "$PROP_FILE" | cut -d"=" -f2 | tr -d " \r\t")
CHANGES=$(grep "^CHANGES_COUNT" "$PROP_FILE" | cut -d"=" -f2 | tr -d " \r\t")

if [ -z "$MAJOR" ] || [ -z "$MINOR" ] || [ -z "$PATCH" ]; then
    echo "❌ Error: No se pudieron parsear las variables de versión en $PROP_FILE"
    read -p "Presiona Enter para cerrar..."
    exit 1
fi

VERSION_TAG="v${MAJOR}.${MINOR}.${PATCH}"
APK_NAME="FinanceFlow-${VERSION_TAG}.apk"

echo "🚀 Procesando versión: $VERSION_TAG (Cambios acumulados: ${CHANGES:-0}/10)"

POSSIBLE_PATHS=(
    "$PROJECT_DIR/release/release/app-release.apk"
    "$PROJECT_DIR/release/app-release.apk"
    "$PROJECT_DIR/app/build/outputs/apk/release/app-release.apk"
)

SOURCE_APK=""
for path in "${POSSIBLE_PATHS[@]}"; do
    if [ -f "$path" ]; then
        SOURCE_APK="$path"
        break
    fi
done

if [ -z "$SOURCE_APK" ]; then
    echo "❌ Error: No se encontró app-release.apk en las rutas de compilación."
    echo "Compila la APK en Android Studio antes de ejecutar este script."
    read -p "Presiona Enter para cerrar..."
    exit 1
fi

FINAL_RELEASE_DIR="$PROJECT_DIR/release"
mkdir -p "$FINAL_RELEASE_DIR"
TARGET_APK="$FINAL_RELEASE_DIR/$APK_NAME"

cp -f "$SOURCE_APK" "$TARGET_APK"
echo "📦 APK renombrada a: $APK_NAME"

git config user.email "logichive.dev@gmail.com"
git config user.name "logichivedev-gif"

echo "📤 Subiendo cambios de código a GitHub..."
git add .
git commit -m "Release ${VERSION_TAG} - Actualización de binarios (Cambios: ${CHANGES:-0}/10)" --allow-empty
git push origin main

if [ -f "$NOTES_FILE" ]; then
    RELEASE_NOTES=$(cat "$NOTES_FILE")
    echo "📄 Notas cargadas desde RELEASE_NOTES.md"
else
    RELEASE_NOTES="Release automática de **$VERSION_TAG**."
fi

echo "🌐 Publicando Release en GitHub..."
if gh release view "$VERSION_TAG" >/dev/null 2>&1; then
    echo "🔄 Actualizando versión existente..."
    gh release upload "$VERSION_TAG" "$TARGET_APK" --clobber
    gh release edit "$VERSION_TAG" --notes "$RELEASE_NOTES"
else
    echo "✨ Creando nueva release..."
    gh release create "$VERSION_TAG" "$TARGET_APK" --title "FinanceFlow $VERSION_TAG" --notes "$RELEASE_NOTES"
fi

echo "✅ ¡Publicación de $VERSION_TAG completada con éxito en GitHub!"
read -p "Presiona Enter para cerrar..."
