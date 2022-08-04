#!/bin/bash

if [[ $# -ne 1 ]]; then
    echo "usage: $0 <db_name>"
    exit 1
fi

# https://stackoverflow.com/a/36591842
psql -tc "SELECT 1 FROM pg_database WHERE datname = '$1'" | grep -q 1 || psql -c "CREATE DATABASE $1"
