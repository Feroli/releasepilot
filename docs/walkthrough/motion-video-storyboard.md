# ReleasePilot Walkthrough Motion Video Storyboard

This storyboard describes the generated challenge walkthrough video in
`deliverables/walkthrough/releasepilot-walkthrough.mp4`.

## Target Runtime

~3:28. Fast-paced with continuous motion; hard maximum under 10 minutes.

## Style

"Neon Deploy" — deep indigo base with electric cyan → violet → magenta gradients and lime
success accents, glow/bloom, a persistent drifting particle field, gentle camera drift, and
quick light-sweep transitions between scenes. 1080p, 30fps. A synthetic voiceover (macOS `say`,
timed to the cues) plays over burned-in animated subtitles.

## Scene Plan

| # | Start | Dur | Visual | Message |
| --- | --- | --- | --- | --- |
| 1 | 0:00 | 9s | Title + glowing `dev → staging → prod` rail, travelling packet | ReleasePilot moves versions safely through the pipeline |
| 2 | 0:09 | 10s | Three env nodes, green "allowed" arc, red "no skipping" arc, guarantee pills | One safe step at a time; approved, tracked, reversible |
| 3 | 0:19 | 11s | Central Promotion node with orbiting responsibility chips | Promotion is the aggregate and owns the lifecycle |
| 4 | 0:30 | 13s | 3×2 grid mapping DDD building blocks to real classes | DDD is the point, and it is evidenced in the code |
| 5 | 0:43 | 15s | State nodes with a token walking the happy path; cancel/rollback branches | The strict lifecycle state machine |
| 6 | 0:58 | 12s | Five numbered invariant rows with enforcement mechanism per row | Where the rules live and how each is enforced |
| 7 | 1:10 | 13s | Nested hexagons (domain / application) with inbound and infra adapters | Hexagonal ports/adapters keep the core pure |
| 8 | 1:23 | 10s | Write column vs read column converging on a PostgreSQL cylinder | CQRS command/query separation |
| 9 | 1:33 | 14s | Sequence-diagram lifelines with animated messages and a transaction box | One command, one transaction, immediate response |
| 10 | 1:47 | 13s | API → outbox → publisher → RabbitMQ → audit/notification, looping packets | Respond first, process events asynchronously |
| 11 | 2:00 | 11s | ER table cards with FK connectors and a partial-unique-index code card | PostgreSQL write model and the concurrency guard |
| 12 | 2:11 | 11s | Application core wired to four `«interface»` ports, each to a stub adapter | Ports with replaceable stub adapters |
| 13 | 2:22 | 10s | Domain error → HTTP status rows with coloured status pills | Explicit failure mapping (409, 403, 404, …) |
| 14 | 2:32 | 14s | Request JSON card + response card whose status and history update live | Request, drive the lifecycle, query state history |
| 15 | 2:46 | 10s | Three-tier test pyramid with per-tier detail | Domain, API, and Testcontainers coverage |
| 16 | 2:56 | 12s | Four trade-off cards (chosen vs alternative + reason) | Deliberate design trade-offs |
| 17 | 3:08 | 10s | Vertical roadmap with a descending packet | Future improvements |
| 18 | 3:18 | 10s | Wordmark, terminal run card, stack recap chips | How to run it locally; recap |

## Generation Approach

The video is generated from repo-owned source, not hand-edited in an external tool:

- Animated scenes: `scripts/walkthrough/web` — one 1920×1080 SVG stage plus an HTML overlay,
  where every animated property is a pure function of `window.seek(t)` (no wall-clock CSS
  animation), so frames are fully deterministic.
- Capture: `scripts/walkthrough/render.mjs` serves the web app over localhost and screenshots
  each frame through headless Chromium (Playwright).
- Voiceover: `scripts/walkthrough/gen-voiceover.mjs` speaks only the subtitle text (via
  `spokenText`, which strips styling tags and turns glyphs into pauses), one line at a time,
  sequenced so clips never overlap, then mixes them onto one track (set `VOICEOVER=0` to skip).
- Encode: ffmpeg stitches the frames into a 1080p H.264 MP4 and muxes the narration.
- Entry point: `scripts/generate-walkthrough-video.sh` (requires `node`, `npm`, `ffmpeg`; the
  voiceover additionally needs macOS `say`).

Narration text (on-screen `html` and spoken `say`) lives in
`scripts/walkthrough/web/js/captions.js`, defined per scene with scene-local times so inserting
or resizing a scene keeps subtitles and voiceover aligned automatically. It is mirrored in
`docs/walkthrough/motion-video-script.md`.
