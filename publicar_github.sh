#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
konsole --hold -e bash -c "cd \"$SCRIPT_DIR\" && ./publish.sh"
