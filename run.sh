#!/usr/bin/env bash

#-------------------------------------------------------------------------
# cobalt.directory builder
#-------------------------------------------------------------------------
# Usage: ./build.sh <jekyll_env> <output_dir>
#   <jekyll_env>  – value for the JEKYLL_ENV variable (e.g. production|development)
#   <output_dir>  – directory where the built site will be copied to
#-------------------------------------------------------------------------

set -euo pipefail

# ------------------------- Arguments ----------------------------
if [[ $# -ne 2 ]]; then
  echo "Error: expected exactly 2 arguments." >&2
  echo "Usage: $0 <jekyll_env> <output_dir>" >&2
  exit 1
fi

export JEKYLL_ENV="$1"
OUTPUT_DIR="$2"

# ------------------------- Paths -------------------------------
PROJECT_ROOT="$(pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
WEB_DIR="$PROJECT_ROOT/web"

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
require jekyll
require bundle

# Java ≥ 21
JAVA_MAJOR="$(java -version 2>&1 | awk -F'[\".]' '/version/ {print $2}')"
if (( JAVA_MAJOR < 21 )); then
  echo "Java 21 or newer is required (found ${JAVA_MAJOR})." >&2
  exit 1
fi

# Ruby gems path
export GEM_HOME="$HOME/gems"
export PATH="$GEM_HOME/bin:$PATH"

# ------------------------- Build Jar -----------------------------
git pull --ff-only
cd "$BACKEND_DIR"
mvn -q clean package
cp "$BACKEND_DIR/target/cobaltdirectory-latest.jar" .

# ------------------------- Run Jar -----------------------------
java -jar cobaltdirectory-latest.jar web

# ------------------------- Jekyll Build ---------------------------
cd "$WEB_DIR"
bundle install --quiet
bundle exec jekyll build

# ------------------------- Copy Output -------------------------
mkdir -p "$OUTPUT_DIR"
# clean target directory but preserve it
find "$OUTPUT_DIR" -mindepth 1 -delete
cp -r "_site/"* "$OUTPUT_DIR/"

# ------------------------- cleanup -------------------------------------
rm -rf "$WEB_DIR/.jekyll-cache"