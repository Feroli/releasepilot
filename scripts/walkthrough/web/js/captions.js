// Narration track. Defined per scene with scene-LOCAL times, then expanded to
// absolute cues from the scene schedule — so inserting or resizing a scene keeps
// everything aligned automatically.
//
// `html` is the single source of truth for BOTH the on-screen subtitle and the
// spoken voiceover: gen-voiceover.mjs speaks exactly this text via spokenText()
// (tags stripped, glyphs turned into pauses). The voiceover never reads anything
// else that is on screen.

import { clamp, easeOutCubic } from "./ease.js";
import { SCENES } from "./timeline.js";

const A = (s) => `<span class="accent">${s}</span>`; // cyan
const B = (s) => `<span class="accent2">${s}</span>`; // lime
const C = (s) => `<span class="accent3">${s}</span>`; // magenta

const SCRIPT = {
  intro: [
    { s: 0.5, e: 4.4, html: `ReleasePilot moves application versions through ${A("dev → staging → production")}.` },
    { s: 4.6, e: 8.7, html: `A backend built for ${B("safe, auditable, reversible")} promotions.` },
  ],
  problem: [
    { s: 0.3, e: 4.4, html: `One version. One step at a time. No skipping environments.` },
    { s: 4.6, e: 9.7, html: `Every move is ${A("approved, tracked, and reversible")}.` },
  ],
  aggregate: [
    { s: 0.3, e: 4.7, html: `The core decision: ${A("Promotion is the aggregate")}.` },
    { s: 4.9, e: 10.7, html: `It owns the lifecycle — not the controller, not the database.` },
  ],
  ddd: [
    { s: 0.4, e: 5.0, html: `The challenge says it plainly — ${A("model it with DDD")}.` },
    { s: 5.2, e: 9.2, html: `The aggregate guards its rules; a violation is a ${C("domain error, never a 500")}.` },
    { s: 9.4, e: 12.7, html: `Value objects, a domain service, and events complete the model.` },
  ],
  stateMachine: [
    { s: 0.3, e: 4.6, html: `A promotion walks a strict state machine.` },
    { s: 4.8, e: 9.2, html: `${A("Requested → Approved → Deploying → Completed")}.` },
    { s: 9.4, e: 14.7, html: `Any live step can ${C("cancel")}; approved or deploying can ${C("roll back")}.` },
  ],
  invariants: [
    { s: 0.3, e: 4.3, html: `The invariants live ${A("inside the aggregate")}.` },
    { s: 4.5, e: 8.3, html: `No skipping. Finish the previous environment first.` },
    { s: 8.5, e: 11.8, html: `One active promotion per target. Terminal states are ${C("immutable")}.` },
  ],
  hexagon: [
    { s: 0.3, e: 4.6, html: `Hexagonal architecture keeps the domain pure.` },
    { s: 4.8, e: 9.1, html: `Adapters talk to ${A("ports")} — the core never sees HTTP or SQL.` },
    { s: 9.3, e: 12.7, html: `Inbound adapters in, infrastructure out.` },
  ],
  cqrs: [
    { s: 0.3, e: 4.6, html: `Commands and queries are split.` },
    { s: 4.8, e: 9.7, html: `Writes go through the aggregate; reads return ${A("purpose-built DTOs")}.` },
  ],
  commandFlow: [
    { s: 0.3, e: 4.3, html: `A command loads state and calls the aggregate.` },
    { s: 4.5, e: 8.5, html: `It saves the change and the event in ${A("one transaction")}.` },
    { s: 8.7, e: 13.7, html: `The API responds — ${B("201 Created")} — right away.` },
  ],
  asyncFlow: [
    { s: 0.3, e: 4.5, html: `After commit, the ${A("outbox publisher")} ships events to RabbitMQ.` },
    { s: 4.7, e: 8.7, html: `Consumers run asynchronously.` },
    { s: 8.9, e: 12.7, html: `Audit logging never blocks the response.` },
  ],
  persistence: [
    { s: 0.3, e: 4.5, html: `PostgreSQL holds the write model, migrated by Flyway.` },
    { s: 4.7, e: 10.7, html: `A ${A("partial unique index")} enforces one active promotion under concurrency.` },
  ],
  ports: [
    { s: 0.3, e: 4.5, html: `Deployment, issue tracker, notifications, approvers —` },
    { s: 4.7, e: 10.7, html: `all ${A("ports")}. The challenge uses stubs; the core can't tell.` },
  ],
  errors: [
    { s: 0.3, e: 4.5, html: `Domain errors map to precise HTTP status codes.` },
    { s: 4.7, e: 9.7, html: `${C("409")} for bad transitions, ${C("403")} for non-approvers.` },
  ],
  apiDemo: [
    { s: 0.3, e: 4.5, html: `Request a promotion with a simple JSON body.` },
    { s: 4.7, e: 8.7, html: `Approve, deploy, complete —` },
    { s: 8.9, e: 13.7, html: `then query the full ${A("state history")}.` },
  ],
  tests: [
    { s: 0.3, e: 4.5, html: `Tested top to bottom.` },
    { s: 4.7, e: 9.7, html: `Domain units, API tests, and ${A("Testcontainers")} end-to-end.` },
  ],
  tradeoffs: [
    { s: 0.3, e: 4.5, html: `Deliberate trade-offs.` },
    { s: 4.7, e: 8.5, html: `Outbox over direct publish. CQRS without event sourcing.` },
    { s: 8.7, e: 11.9, html: `Fixed pipeline. Stubs that stay replaceable.` },
  ],
  next: [
    { s: 0.3, e: 4.5, html: `What would come next?` },
    { s: 4.7, e: 9.7, html: `Idempotency keys, read projections, real integrations and auth.` },
  ],
  outro: [
    { s: 0.3, e: 4.5, html: `Run it locally: ${A("docker compose up")}, then ${A("./gradlew bootRun")}.` },
    { s: 4.7, e: 9.8, html: `ReleasePilot — thanks for watching.` },
  ],
};

// Exact spoken form of a cue: strip styling tags and drop purely visual glyphs so
// TTS speaks only the words shown in the subtitle — never a symbol or on-screen label.
export function spokenText(html) {
  return html
    .replace(/<[^>]*>/g, "")        // remove styling spans
    .replace(/\.\//g, "")           // "./gradlew" → "gradlew" (path punctuation, not a word)
    .replace(/[→·—–]/g, ", ")       // arrows / dots / dashes → a spoken pause
    .replace(/[;:]/g, ",")          // colons / semicolons → a spoken pause
    .replace(/\s*,\s*,\s*/g, ", ")  // collapse doubled pauses
    .replace(/\s+/g, " ")
    .trim();
}

export const CUES = [];
for (const scene of SCENES) {
  const lines = SCRIPT[scene.id] || [];
  for (const l of lines) {
    CUES.push({ s: scene.start + l.s, e: scene.start + l.e, html: l.html });
  }
}

export function updateCaptions(t) {
  const bar = document.getElementById("subtitle");
  const el = document.getElementById("subtitle-inner");
  const cue = CUES.find((c) => t >= c.s && t < c.e);
  if (!cue) {
    bar.style.opacity = "0";
    return;
  }
  if (el.dataset.k !== String(cue.s)) {
    el.innerHTML = cue.html;
    el.dataset.k = String(cue.s);
  }
  const inT = clamp((t - cue.s) / 0.28);
  const outT = clamp((cue.e - t) / 0.22);
  bar.style.opacity = String(Math.min(inT, outT));
  const rise = (1 - easeOutCubic(inT)) * 16;
  el.style.transform = `translateY(${rise}px)`;
}
