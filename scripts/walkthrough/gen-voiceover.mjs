// Synthesize a narration track that speaks ONLY the subtitle text, one line at a
// time, never overlapping. macOS `say` renders each subtitle line (via
// spokenText); clips are sequenced so no two ever play at once, then mixed.
//
// Usage: node gen-voiceover.mjs <outWav>
// Requires: macOS `say`, ffmpeg/ffprobe.

import { execFileSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";
import os from "node:os";
import { CUES, spokenText } from "./web/js/captions.js";
import { TOTAL } from "./web/js/timeline.js";

const outWav = process.argv[2];
if (!outWav) {
  console.error("usage: node gen-voiceover.mjs <outWav>");
  process.exit(1);
}

const RATE = process.env.VO_RATE || "178";
const GAP = 0.1; // seconds of silence enforced between spoken lines
const PREFERRED = ["Samantha", "Allison", "Ava", "Alex", "Evan", "Tom"];

function pickVoice() {
  try {
    const list = execFileSync("say", ["-v", "?"], { encoding: "utf8" });
    const names = list
      .split("\n")
      .filter((l) => l.includes("en_US"))
      .map((l) => l.split(/\s{2,}/)[0].trim());
    for (const p of PREFERRED) {
      if (names.includes(p)) {
        return p;
      }
    }
  } catch {
    /* fall through to default voice */
  }
  return null;
}

function clipDuration(file) {
  const out = execFileSync(
    "ffprobe",
    ["-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", file],
    { encoding: "utf8" }
  );
  return parseFloat(out.trim()) || 0;
}

function main() {
  const voice = pickVoice();
  console.log(`voice: ${voice || "(system default)"}  rate: ${RATE}  cues: ${CUES.length}`);

  const work = fs.mkdtempSync(path.join(os.tmpdir(), "rp-vo-"));

  // 1. Render each subtitle line to audio and measure it.
  const clips = CUES.map((cue, i) => {
    const spoken = spokenText(cue.html);
    const aiff = path.join(work, `n${String(i).padStart(2, "0")}.aiff`);
    const args = ["-r", RATE, "-o", aiff];
    if (voice) {
      args.push("-v", voice);
    }
    args.push(spoken);
    execFileSync("say", args);
    return { aiff, spoken, cueStart: cue.s, dur: clipDuration(aiff) };
  });

  // 2. Sequence so lines never overlap: start no earlier than the cue, and never
  //    before the previous line has finished (plus a small gap).
  let cursor = 0;
  let pushed = 0;
  const placed = clips.map((c) => {
    const start = Math.max(c.cueStart, cursor);
    if (start > c.cueStart + 0.001) {
      pushed++;
    }
    cursor = start + c.dur + GAP;
    return { aiff: c.aiff, startMs: Math.round(start * 1000), start, spoken: c.spoken };
  });

  console.log("\nschedule (start → line):");
  for (const p of placed) {
    console.log(`  ${p.start.toFixed(1).padStart(6)}s  ${p.spoken}`);
  }
  console.log(`\nlines nudged later to avoid overlap: ${pushed}/${placed.length}; last line ends ~${cursor.toFixed(1)}s of ${TOTAL}s`);

  // 3. Mix onto a single track (no overlaps → clean, sequential speech).
  const inputs = placed.flatMap((c) => ["-i", c.aiff]);
  const delays = placed.map((c, i) => `[${i}:a]adelay=${c.startMs}|${c.startMs}[d${i}]`);
  const mixIns = placed.map((_, i) => `[d${i}]`).join("");
  const filter =
    `${delays.join(";")};` +
    `${mixIns}amix=inputs=${placed.length}:normalize=0:dropout_transition=0[m];` +
    `[m]apad,atrim=0:${TOTAL},alimiter=limit=0.95[out]`;

  execFileSync(
    "ffmpeg",
    ["-y", "-loglevel", "error", ...inputs, "-filter_complex", filter, "-map", "[out]", "-ar", "48000", "-ac", "2", outWav],
    { stdio: "inherit" }
  );

  fs.rmSync(work, { recursive: true, force: true });
  console.log(`\nvoiceover written → ${outWav}`);
}

main();
