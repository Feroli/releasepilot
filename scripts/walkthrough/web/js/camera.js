// Persistent background (grid + drifting particles) and a gentle global camera
// drift, all pure functions of time for continuity across scene cuts.

import { svg, set } from "./dom.js";
import { rand } from "./ease.js";

const PARTICLES = 46;
const COLORS = ["#22d3ee", "#8b5cf6", "#ec4899", "#a3e635"];

export function buildBackground(root) {
  const bg = svg("g", { id: "bg" });

  // faint grid
  const grid = svg("g", { opacity: 0.06, stroke: "#8b93c7", "stroke-width": 1 });
  for (let x = 0; x <= 1920; x += 96) {
    grid.appendChild(svg("line", { x1: x, y1: 0, x2: x, y2: 1080 }));
  }
  for (let y = 0; y <= 1080; y += 96) {
    grid.appendChild(svg("line", { x1: 0, y1: y, x2: 1920, y2: y }));
  }
  bg.appendChild(grid);

  // drifting particles
  const parts = svg("g", { id: "bg-parts" });
  const store = [];
  for (let i = 0; i < PARTICLES; i++) {
    const bx = rand(i + 1) * 1920;
    const by = rand(i + 71) * 1080;
    const r = 1.4 + rand(i + 130) * 3.4;
    const color = COLORS[i % COLORS.length];
    const speed = 8 + rand(i + 200) * 22;
    const c = svg("circle", { cx: bx, cy: by, r, fill: color, opacity: 0.25, filter: "url(#glow-soft)" });
    parts.appendChild(c);
    store.push({ c, bx, by, r, speed, seed: i });
  }
  bg.appendChild(parts);
  root.appendChild(bg);
  return store;
}

export function updateBackground(store, t) {
  for (const p of store) {
    const y = ((p.by - t * p.speed) % 1120 + 1120) % 1120 - 20;
    const tw = 0.18 + 0.22 * (0.5 + 0.5 * Math.sin(t * 0.9 + p.seed * 1.3));
    set(p.c, { cy: y, opacity: tw });
  }
}

// Gentle continuous drift of the whole foreground so nothing feels static.
export function applyCamera(cameraEl, t) {
  const x = Math.sin(t * 0.14) * 7;
  const y = Math.cos(t * 0.11) * 6;
  const s = 1 + 0.004 * (0.5 + 0.5 * Math.sin(t * 0.08));
  set(cameraEl, {
    transform: `translate(${960 + x},${540 + y}) scale(${s}) translate(${-960},${-540})`,
  });
}
