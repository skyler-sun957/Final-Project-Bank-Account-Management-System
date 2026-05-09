#!/usr/bin/env bash
set -euo pipefail
javac -d out src/bank/*.java tests/BankTestRunner.java
java -cp out bank.BankTestRunner
