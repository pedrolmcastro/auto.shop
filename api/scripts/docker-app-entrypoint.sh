#!/usr/bin/env bash

set -euo pipefail

pepper=${PEPPER_FILE-secrets/pepper.bin}

if [[ ! -e "$pepper" && ! -L "$pepper" ]]; then
    (
        temporary=$(mktemp -- "${pepper}.XXXXXX")
        trap 'rm -f -- "$temporary"' EXIT
        openssl rand 32 > "$temporary"
        chmod 400 "$temporary"
        if ! ln -T -- "$temporary" "$pepper" 2>/dev/null && [[ ! -e "$pepper" && ! -L "$pepper" ]]; then
            echo 'Failed to publish the pepper file.' >&2
            exit 1
        fi
    )
fi

if [[ -L "$pepper" || ! -f "$pepper" || ! -r "$pepper" || $(stat -c '%s' "$pepper") -ne 32 ]]; then
    echo 'The pepper file must be a readable regular file of exactly 32 bytes.' >&2
    exit 1
fi

exec java -jar /app/app.jar "$@"
