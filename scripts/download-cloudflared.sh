#!/bin/bash
set -euo pipefail

ASSETS_DIR="app/src/main/assets"
BINARY_NAME="cloudflared"
REPO_URL="https://api.github.com/repos/cloudflare/cloudflared/releases/latest"

mkdir -p "$ASSETS_DIR"

echo "Fetching latest cloudflared download link..."

DOWNLOAD_URL=$(
  curl -fsSL "$REPO_URL" \
  | grep -o '"browser_download_url":[^"]*"[^"]*cloudflared-linux-arm64[^"]*"' \
  | cut -d '"' -f 4 \
  | head -n1
)

if [ -z "${DOWNLOAD_URL:-}" ]; then
  echo "Error: Unable to fetch download link!"
  exit 1
fi

echo "Download URL: $DOWNLOAD_URL"
curl -fsSL -o "$ASSETS_DIR/$BINARY_NAME" "$DOWNLOAD_URL"
chmod +x "$ASSETS_DIR/$BINARY_NAME"

echo "Download successful. File saved to: $ASSETS_DIR/$BINARY_NAME"
else
    echo "Download failed!"
    exit 1
fi
