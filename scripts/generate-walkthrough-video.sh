#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_DIR="$ROOT_DIR/deliverables/walkthrough"
OUTPUT_FILE="$OUTPUT_DIR/releasepilot-walkthrough.mp4"
WORK_DIR="$(mktemp -d)"

cleanup() {
  rm -rf "$WORK_DIR"
}
trap cleanup EXIT

mkdir -p "$OUTPUT_DIR"

javac -d "$WORK_DIR/classes" "$ROOT_DIR/scripts/walkthrough/GenerateWalkthroughFrames.java"
java -Djava.awt.headless=true -cp "$WORK_DIR/classes" GenerateWalkthroughFrames "$WORK_DIR/frames"

inputs=()
filters=()
concat_inputs=""
for index in $(seq 1 12); do
  frame="$WORK_DIR/frames/slide$(printf '%02d' "$index").png"
  inputs+=("-framerate" "30" "-loop" "1" "-t" "25" "-i" "$frame")
  zero_based=$((index - 1))
  filters+=("[$zero_based:v]scale=1344:756,crop=1280:720:x='32+32*sin(t*0.12)':y='18+18*cos(t*0.10)',fps=30,fade=t=in:st=0:d=0.8,fade=t=out:st=24.2:d=0.8,setsar=1[v$zero_based]")
  concat_inputs+="[v$zero_based]"
done

filter_complex="$(IFS=';'; echo "${filters[*]}");${concat_inputs}concat=n=12:v=1:a=0[v]"

ffmpeg -y \
  "${inputs[@]}" \
  -filter_complex "$filter_complex" \
  -map "[v]" \
  -an \
  -c:v libx264 \
  -preset veryfast \
  -crf 28 \
  -pix_fmt yuv420p \
  "$OUTPUT_FILE"

ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "$OUTPUT_FILE"
