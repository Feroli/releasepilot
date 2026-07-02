#!/usr/bin/env bash
# Regenerates deliverables/walkthrough/releasepilot-walkthrough.mp4 from repo-owned
# source: an animated web app (scripts/walkthrough/web) captured frame-by-frame via
# headless Chromium and encoded to 1080p H.264 with ffmpeg.
#
# Requirements: node, npm, ffmpeg. Playwright downloads Chromium on first `npm install`.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WALK_DIR="$ROOT_DIR/scripts/walkthrough"
OUTPUT_DIR="$ROOT_DIR/deliverables/walkthrough"
OUTPUT_FILE="$OUTPUT_DIR/releasepilot-walkthrough.mp4"
FPS="${FPS:-30}"
FRAMES_DIR="$(mktemp -d)"

cleanup() {
  rm -rf "$FRAMES_DIR"
}
trap cleanup EXIT

mkdir -p "$OUTPUT_DIR"

echo "==> Installing renderer dependencies"
cd "$WALK_DIR"
if [ ! -d node_modules/playwright ]; then
  npm install --no-audit --no-fund
fi
# Ensure a Chromium is available for Playwright (skips download if already cached).
npx playwright install chromium >/dev/null 2>&1 || true

echo "==> Rendering frames"
node render.mjs "$FRAMES_DIR" "$FPS"

# Synthetic voiceover (macOS `say`). Set VOICEOVER=0 to force a silent track.
NARRATION=""
if [ "${VOICEOVER:-1}" != "0" ] && command -v say >/dev/null 2>&1; then
  echo "==> Synthesizing voiceover"
  NARRATION="$FRAMES_DIR/narration.wav"
  if ! node gen-voiceover.mjs "$NARRATION"; then
    echo "voiceover generation failed; encoding silent"
    NARRATION=""
  fi
fi

echo "==> Encoding MP4 (1080p H.264)"
if [ -n "$NARRATION" ]; then
  ffmpeg -y \
    -framerate "$FPS" -i "$FRAMES_DIR/frame_%05d.png" \
    -i "$NARRATION" \
    -c:v libx264 -preset medium -crf 20 -pix_fmt yuv420p \
    -c:a aac -b:a 160k \
    -shortest -movflags +faststart \
    "$OUTPUT_FILE"
else
  ffmpeg -y \
    -framerate "$FPS" -i "$FRAMES_DIR/frame_%05d.png" \
    -f lavfi -i anullsrc=channel_layout=stereo:sample_rate=48000 \
    -c:v libx264 -preset medium -crf 20 -pix_fmt yuv420p \
    -c:a aac -b:a 96k \
    -shortest -movflags +faststart \
    "$OUTPUT_FILE"
fi

echo "==> Result"
ffprobe -v error -show_entries format=duration:stream=width,height,codec_name -of default=noprint_wrappers=1 "$OUTPUT_FILE"
ls -lh "$OUTPUT_FILE"
