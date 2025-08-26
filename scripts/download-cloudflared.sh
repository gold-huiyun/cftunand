#!/bin/bash

ASSETS_DIR="app/src/main/assets"
BINARY_NAME="cloudflared"
REPO_URL="https://api.github.com/repos/cloudflare/cloudflared/releases/latest"

# 确保目录存在
mkdir -p "$ASSETS_DIR"

echo "Fetching latest cloudflared download link..."
DOWNLOAD_URL=$(curl -s $REPO_URL | grep "browser_download_url" | grep "linux-arm64" | cut -d '"' -f 4)

if [ -z "$DOWNLOAD_URL" ]; then
    echo "Error: Unable to fetch download link!"
    exit 1
fi

echo "Download URL: $DOWNLOAD_URL"
wget -O "$ASSETS_DIR/$BINARY_NAME" "$DOWNLOAD_URL"

if [ $? -eq 0 ]; then
    echo "Download successful. File saved to: $ASSETS_DIR/$BINARY_NAME"
else
    echo "Download failed!"
    exit 1
fi
