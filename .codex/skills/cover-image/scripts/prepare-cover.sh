#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <input-image> <output.png|output.jpg>" >&2
  exit 2
fi

input=$1
output=$2
max_bytes=409600

if ! command -v ffmpeg >/dev/null 2>&1; then
  echo "Error: ffmpeg is required to prepare cover images." >&2
  exit 1
fi

if [[ ! -f "$input" ]]; then
  echo "Error: input image does not exist: $input" >&2
  exit 1
fi

mkdir -p "$(dirname "$output")"
tmp_dir=$(mktemp -d)
trap 'rm -rf "$tmp_dir"' EXIT

sizes=("1200:900" "1000:750" "800:600")
extension=${output##*.}
extension=$(printf '%s' "$extension" | tr '[:upper:]' '[:lower:]')

if [[ "$extension" == "jpg" || "$extension" == "jpeg" ]]; then
  qualities=(2 3 4 5 6 7 8)
  for size in "${sizes[@]}"; do
    for quality in "${qualities[@]}"; do
      candidate="$tmp_dir/cover-${size/:/x}-q${quality}.jpg"
      ffmpeg -loglevel error -y -i "$input" \
        -vf "scale=${size}:force_original_aspect_ratio=increase,crop=${size}" \
        -frames:v 1 -q:v "$quality" "$candidate"

      bytes=$(wc -c < "$candidate" | tr -d ' ')
      if (( bytes < max_bytes )); then
        cp "$candidate" "$output"
        echo "Prepared $output (${size/:/x}, ${bytes} bytes, JPEG q:v ${quality})"
        exit 0
      fi
    done
  done
elif [[ "$extension" == "png" ]]; then
  colors=(256 192 128 96 64)
  for size in "${sizes[@]}"; do
    for color_count in "${colors[@]}"; do
      candidate="$tmp_dir/cover-${size/:/x}-${color_count}.png"
      ffmpeg -loglevel error -y -i "$input" \
        -filter_complex \
        "[0:v]scale=${size}:force_original_aspect_ratio=increase,crop=${size},split[a][b];[a]palettegen=max_colors=${color_count}:stats_mode=diff[p];[b][p]paletteuse=dither=bayer:bayer_scale=3" \
        -frames:v 1 -compression_level 9 "$candidate"

      bytes=$(wc -c < "$candidate" | tr -d ' ')
      if (( bytes < max_bytes )); then
        cp "$candidate" "$output"
        echo "Prepared $output (${size/:/x}, ${bytes} bytes, ${color_count} colors)"
        exit 0
      fi
    done
  done
else
  echo "Error: output extension must be .png, .jpg, or .jpeg." >&2
  exit 2
fi

echo "Error: unable to produce a 4:3 image smaller than ${max_bytes} bytes." >&2
exit 1
