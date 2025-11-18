#!/bin/bash

echo "🛑 Stopping backend..."
lsof -ti:8088 | xargs kill -9 2>/dev/null
sleep 2

echo "🧹 Cleaning..."
cd "$(dirname "$0")"
./gradlew clean

echo "🔨 Building..."
./gradlew build -x test

echo "🚀 Starting backend..."
./gradlew bootRun

