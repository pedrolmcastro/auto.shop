#!/usr/bin/env bash

set -euo pipefail
set -o noclobber

API_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
PEPPER_DIR="$API_DIR/secrets"
PEPPER_FILE="$PEPPER_DIR/pepper.bin"

umask 077
mkdir -p -- "$PEPPER_DIR"

if [[ -L "$PEPPER_DIR" || ! -O "$PEPPER_DIR" ]]; then
    echo 'O diretório do pepper deve pertencer ao usuário atual e não pode ser um link simbólico.' >&2
    exit 1
fi

chmod 700 -- "$PEPPER_DIR"
openssl rand 32 > "$PEPPER_FILE"
chmod 444 -- "$PEPPER_FILE"
