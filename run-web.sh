#!/usr/bin/env bash

set -euo pipefail

# ------------------------- Arguments ----------------------------
if [[ $# -ne 1 ]]; then
  echo "Error: Missing args." >&2
  echo "Usage: $0 <web_dir>" >&2
  exit 1
fi

WEB_DIR="$1"
echo "Web directory is $1"

# ------------------------- Paths -------------------------------
PROJECT_ROOT="$(pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"

# ------------------------- Dependencies --------------------------

require() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing dependency: $1" >&2
    exit 1
  }
}
require git
require mvn
require java

# Java ≥ 21
JAVA_MAJOR="$(java -version 2>&1 | awk -F'[\".]' '/version/ {print $2}')"
if (( JAVA_MAJOR < 21 )); then
  echo "Java 21 or newer is required (found ${JAVA_MAJOR})." >&2
  exit 1
fi

# ------------------------- Build Jar -----------------------------
git pull --ff-only
cd "$BACKEND_DIR"
echo "Building jar..."
mvn -q clean package
echo "Moving jar to $BACKEND_DIR"
cp "$BACKEND_DIR/target/cobaltdirectory-latest.jar" .

# ------------------------- Run Jar -----------------------------
echo "Running $BACKEND_DIR/cobaltdirectory-latest.jar"
java -jar cobaltdirectory-latest.jar

# ------------------------- Jekyll Build ---------------------------
echo "Copying JSON output into web directory..."
cp "$BACKEND_DIR/api.json $WEB_DIR/static/"
cp "$BACKEND_DIR/api_frontends.json $WEB_DIR/static/"
cp "$BACKEND_DIR/results.json $WEB_DIR/data/"
bundle exec jekyll build