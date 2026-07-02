import { svg, set } from "../dom.js";
import { text, packet } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutCubic, lerp } from "../ease.js";

const ITEMS = [
  { str: "Request idempotency keys", note: "safe client retries", color: "#22d3ee" },
  { str: "Materialized read projections", note: "separate, optimized query tables", color: "#8b5cf6" },
  { str: "Richer deployment failure modeling", note: "partial failures, retries, timeouts", color: "#fbbf24" },
  { str: "Real integrations + authentication", note: "replace stubs, secure the endpoints", color: "#ec4899" },
];
const X = 360;
const Y0 = 360;
const GAP = 128;

export function next() {
  let head, track, rows, dot;
  return {
    id: "next",
    dur: 10,
    build(layer) {
      head = heading(layer, { kicker: "What's next", title: "Where I'd take it" });

      track = svg("line", { x1: X, y1: Y0 - 6, x2: X, y2: Y0 + (ITEMS.length - 1) * GAP + 6, stroke: "rgba(139,129,216,0.5)", "stroke-width": 3, "stroke-dasharray": "6 10", opacity: 0 });
      layer.appendChild(track);

      rows = ITEMS.map((it, i) => {
        const y = Y0 + i * GAP;
        const g = svg("g", { opacity: 0 });
        g.appendChild(svg("circle", { cx: X, cy: y, r: 12, fill: it.color, filter: "url(#glow-strong)" }));
        text(g, { x: X + 44, y: y - 4, str: "›", size: 30, weight: 800, fill: it.color });
        text(g, { x: X + 80, y: y + 6, str: it.str, size: 32, weight: 700, fill: "#f5f7ff" });
        text(g, { x: X + 80, y: y + 40, str: it.note, size: 21, weight: 500, fill: "#9aa3c7" });
        layer.appendChild(g);
        return { g, y };
      });

      dot = packet(layer, { r: 9, color: "#ffffff", glow: "glow-huge" });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      set(track, { opacity: easeOutCubic(clamp((lt - 0.6) / 0.6)) });
      rows.forEach((r, i) => {
        const p = clamp((lt - 1.0 - i * 0.6) / 0.6);
        set(r.g, { opacity: clamp(p * 1.3), transform: `translate(${(1 - easeOutCubic(p)) * 50},0)` });
      });
      const dp = clamp((lt - 1.0) / 5.2);
      if (dp > 0 && dp < 1) {
        set(dot, { opacity: 1, cx: X, cy: lerp(Y0, Y0 + (ITEMS.length - 1) * GAP, easeOutCubic(dp)) });
      } else {
        set(dot, { opacity: 0 });
      }
    },
  };
}
