#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <input-image-or-svg> <output.jpg>" >&2
  exit 2
fi

input=$1
output=$2
max_bytes=307200

if ! command -v ffmpeg >/dev/null 2>&1; then
  echo "Error: ffmpeg is required to prepare illustrations." >&2
  exit 1
fi

if [[ ! -f "$input" ]]; then
  echo "Error: input file does not exist: $input" >&2
  exit 1
fi

extension=${output##*.}
extension=$(printf '%s' "$extension" | tr '[:upper:]' '[:lower:]')
if [[ "$extension" != "jpg" && "$extension" != "jpeg" ]]; then
  echo "Error: output extension must be .jpg or .jpeg." >&2
  exit 2
fi

mkdir -p "$(dirname "$output")"
tmp_dir=$(mktemp -d)
trap 'rm -rf "$tmp_dir"' EXIT

source_input=$input
input_extension=${input##*.}
input_extension=$(printf '%s' "$input_extension" | tr '[:upper:]' '[:lower:]')

if [[ "$input_extension" == "svg" ]]; then
  if ! command -v rsvg-convert >/dev/null 2>&1; then
    echo "Error: rsvg-convert is required for SVG illustrations." >&2
    exit 1
  fi

  source_input="$tmp_dir/source.png"
  rsvg-convert --background-color white --width 1600 --height 900 \
    "$input" --output "$source_input"
fi

sizes=("1600:900" "1280:720" "1024:576")
qualities=(2 3 4 5 6 7 8 10 12)

for size in "${sizes[@]}"; do
  for quality in "${qualities[@]}"; do
    candidate="$tmp_dir/illustration-${size/:/x}-q${quality}.jpg"
    ffmpeg -loglevel error -y -i "$source_input" \
      -vf "scale=${size}:force_original_aspect_ratio=decrease,pad=${size}:(ow-iw)/2:(oh-ih)/2:color=white" \
      -frames:v 1 -q:v "$quality" "$candidate"

    bytes=$(wc -c < "$candidate" | tr -d ' ')
    if (( bytes < max_bytes )); then
      cp "$candidate" "$output"
      echo "Prepared $output (${size/:/x}, ${bytes} bytes, JPEG q:v ${quality})"
      exit 0
    fi
  done
done

echo "Error: unable to produce a readable 16:9 JPEG smaller than ${max_bytes} bytes." >&2
exit 1
