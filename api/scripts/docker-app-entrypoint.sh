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

private_key=${JWT_PRIVATE_KEY_FILE-secrets/jwt-private.pem}
public_key=${JWT_PUBLIC_KEY_FILE-secrets/jwt-public.pem}

if [[ ! -e "$private_key" && ! -L "$private_key" ]]; then
    if [[ -e "$public_key" || -L "$public_key" ]]; then
        echo 'The JWT private key is missing while the public key already exists.' >&2
        exit 1
    fi

    (
        temporary=$(mktemp -- "${private_key}.XXXXXX")
        trap 'rm -f -- "$temporary"' EXIT
        openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$temporary" 2>/dev/null
        chmod 400 "$temporary"
        if ! ln -T -- "$temporary" "$private_key" 2>/dev/null && [[ ! -e "$private_key" && ! -L "$private_key" ]]; then
            echo 'Failed to publish the JWT private key.' >&2
            exit 1
        fi
    )
fi

if [[ -L "$private_key" || ! -f "$private_key" || ! -r "$private_key" ]]; then
    echo 'The JWT private key must be a readable regular file.' >&2
    exit 1
fi

if [[ $(head -n 1 -- "$private_key") != '-----BEGIN PRIVATE KEY-----' ]]; then
    echo 'The JWT private key must be a PKCS#8 PEM file.' >&2
    exit 1
fi

if ! openssl rsa -in "$private_key" -passin pass: -check -noout >/dev/null 2>&1; then
    echo 'The JWT private key must be a valid unencrypted RSA key.' >&2
    exit 1
fi

bits=$(openssl rsa -in "$private_key" -passin pass: -noout -text 2>/dev/null | sed -n 's/^Private-Key: (\([0-9][0-9]*\) bit.*/\1/p')
if [[ -z "$bits" ]] || (( bits < 2048 )); then
    echo 'The JWT private key must be an RSA key of at least 2048 bits.' >&2
    exit 1
fi

if [[ ! -e "$public_key" && ! -L "$public_key" ]]; then
    (
        temporary=$(mktemp -- "${public_key}.XXXXXX")
        trap 'rm -f -- "$temporary"' EXIT
        openssl pkey -in "$private_key" -passin pass: -pubout -out "$temporary"
        chmod 444 "$temporary"
        if ! ln -T -- "$temporary" "$public_key" 2>/dev/null && [[ ! -e "$public_key" && ! -L "$public_key" ]]; then
            echo 'Failed to publish the JWT public key.' >&2
            exit 1
        fi
    )
fi

if [[ -L "$public_key" || ! -f "$public_key" || ! -r "$public_key" ]]; then
    echo 'The JWT public key must be a readable regular file.' >&2
    exit 1
fi

if [[ $(head -n 1 -- "$public_key") != '-----BEGIN PUBLIC KEY-----' ]]; then
    echo 'The JWT public key must be an X.509 PEM file.' >&2
    exit 1
fi

if ! openssl pkey -pubin -in "$public_key" -pubcheck -noout >/dev/null 2>&1; then
    echo 'The JWT public key must be a valid public key.' >&2
    exit 1
fi

private_public_digest=$(openssl pkey -in "$private_key" -passin pass: -pubout -outform DER | openssl dgst -sha256)
public_digest=$(openssl pkey -pubin -in "$public_key" -outform DER | openssl dgst -sha256)

if [[ "$private_public_digest" != "$public_digest" ]]; then
    echo 'The JWT public key does not match the private key.' >&2
    exit 1
fi

exec java -jar /app/app.jar "$@"
