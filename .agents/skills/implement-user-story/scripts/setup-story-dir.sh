#!/bin/bash
STORY_ID=$1
if [ -z "$STORY_ID" ]; then
  echo "Error: No story ID provided."
  exit 1
fi
mkdir -p "docs/$STORY_ID"
echo "Directory docs/$STORY_ID created successfully."
