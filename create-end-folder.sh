#!/bin/bash

folderName="skill09-2025"
desktopPath="$HOME/Desktop"
folderPath="$desktopPath/$folderName"

mkdir -p "$folderPath"

if [ -d "$desktopPath/releases" ]; then
    cp -r "$desktopPath/releases" "$folderPath/releases"
fi

rsync -av --exclude='.gradle' --exclude='build' --exclude='bin' "$(pwd)/" "$folderPath/source"

[ -f README.md ] && cp README.md "$folderPath"
[ -f "$desktopPath/competitor.json" ] && cp "$desktopPath/competitor.json" "$folderPath"

zip -r "$desktopPath/$folderName.zip" "$desktopPath/$folderName"

echo "Task completed successfully."