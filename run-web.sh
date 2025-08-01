#!/usr/bin/env bash

set -euo pipefail

# ------------------------- Arguments ----------------------------
if [[ $# -ne 2 ]]; then
  echo "Error: Missing args." >&2
  echo "Usage: $0 <output_dir> production/development" >&2
  exit 1
fi

OUTPUT_DIR="$1"
export JEKYLL_ENV="$2"
echo "Web output is $1"

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
echo "Building jar..."
mvn -q clean package
echo "Moving jar to $BACKEND_DIR"
cp "$BACKEND_DIR/target/cobaltdirectory-latest.jar" .

# ------------------------- Run Jar -----------------------------
echo "Running $BACKEND_DIR/cobaltdirectory-latest.jar"
java -jar cobaltdirectory-latest.jar web=true

# ------------------------- Jekyll Build ---------------------------
cd "$WEB_DIR"
echo "Installing Gems..."
bundle install --quiet
echo "Building Jekyll site..."
cp "$BACKEND_DIR/api.json" .
bundle exec jekyll build

# ------------------------- Copy Output -------------------------
mkdir -p "$OUTPUT_DIR"
# clean target directory but preserve it
find "$OUTPUT_DIR" -mindepth 1 -delete
echo "Moving build output to $OUTPUT_DIR"
cp -r "_site/"* "$OUTPUT_DIR/"

# ------------------------- Cleanup -------------------------------------
rm -rf "$WEB_DIR/.jekyll-cache"