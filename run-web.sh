#!/usr/bin/env bash

set -euo pipefail

# ------------------------- Arguments ----------------------------
if [[ $# -ne 1 ]]; then
  echo "Error: Missing web output path." >&2
  echo "Usage: $0 <output_dir>" >&2
  exit 1
fi

OUTPUT_DIR="$1"

# ------------------------- Paths -------------------------------
PROJECT_ROOT="$(pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
WEB_DIR="$PROJECT_ROOT/web"

# ------------------------- Dependencies --------------------------
# Ruby gems path
export GEM_HOME="$HOME/gems"
export PATH="$GEM_HOME/bin:$PATH"

require() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing dependency: $1" >&2
    exit 1
  }
}
require git
require mvn
require java
require bundle

# Java ≥ 21
JAVA_MAJOR="$(java -version 2>&1 | awk -F'[\".]' '/version/ {print $2}')"
if (( JAVA_MAJOR < 21 )); then
  echo "Java 21 or newer is required (found ${JAVA_MAJOR})." >&2
  exit 1
fi

# ------------------------- Build Jar -----------------------------
git pull --ff-only
cd "$BACKEND_DIR"
mvn -q clean package
cp "$BACKEND_DIR/target/cobaltdirectory-latest.jar" .

# ------------------------- Run Jar -----------------------------
java -jar cobaltdirectory-latest.jar

# ------------------------- Jekyll Build ---------------------------
cd "$WEB_DIR"
bundle install --quiet
bundle exec jekyll build

# ------------------------- Copy Output -------------------------
mkdir -p "$OUTPUT_DIR"
# clean target directory but preserve it
find "$OUTPUT_DIR" -mindepth 1 -delete
cp -r "_site/"* "$OUTPUT_DIR/"

# ------------------------- Cleanup -------------------------------------
rm -rf "$WEB_DIR/.jekyll-cache"