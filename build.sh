#!/usr/bin/env bash
set -euo pipefail
javac -d out src/bank/*.java
java -cp out bank.Main
