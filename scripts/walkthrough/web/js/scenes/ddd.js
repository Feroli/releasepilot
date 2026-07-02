import { svg, set } from "../dom.js";
import { panel, text } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutBack } from "../ease.js";

// Each DDD tactical building block mapped to the real artifact in the codebase.
const BLOCKS = [
  { concept: "Aggregate root", code: "Promotion", note: "guards its own invariants", color: "#8b5cf6" },
  { concept: "Value objects", code: "Environment · PromotionEligibility", note: "immutable, self-validating", color: "#22d3ee" },
  { concept: "Domain service", code: "EnvironmentPipeline", note: "the one-step rule", color: "#a3e635" },
  { concept: "Domain events", code: "PromotionDomainEvent", note: "recorded on each transition", color: "#fbbf24" },
  { concept: "Domain errors", code: "DomainException", note: "a domain error, never a 500", color: "#ec4899" },
  { concept: "Ubiquitous language", code: "promotion · catalog · releasenotes", note: "bounded contexts", color: "#3b82f6" },
];
const COLS = [140, 700, 1260];
const ROWS = [326, 556];
const W = 520;
const H = 200;

export function ddd() {
  let head, cards;
  return {
    id: "ddd",
    dur: 13,
    build(layer) {
      head = heading(layer, { kicker: "The domain is the point", title: "Domain-Driven Design, in the code" });
      cards = BLOCKS.map((b, i) => {
        const x = COLS[i % 3];
        const y = ROWS[Math.floor(i / 3)];
        const g = svg("g", { opacity: 0 });
        panel(g, { x, y, w: W, h: H, rx: 18, fill: "rgba(18,14,44,0.85)", stroke: b.color, strokeW: 1.6, glow: "glow-soft" });
        g.appendChild(svg("rect", { x, y, width: W, height: 5, rx: 3, fill: b.color, opacity: 0.9 }));
        text(g, { x: x + 34, y: y + 56, str: b.concept, size: 29, weight: 750, fill: b.color });
        text(g, { x: x + 34, y: y + 108, str: b.code, size: 21, weight: 600, fill: "#f5f7ff", family: "var(--mono)" });
        text(g, { x: x + 34, y: y + 152, str: b.note, size: 19, weight: 500, fill: "#9aa3c7" });
        layer.appendChild(g);
        return { g, x, y };
      });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      cards.forEach((c, i) => {
        const p = clamp((lt - 0.7 - i * 0.42) / 0.6);
        const s = 0.9 + 0.1 * easeOutBack(p);
        set(c.g, { opacity: clamp(p * 1.3), transform: `translate(${c.x + W / 2},${c.y + H / 2}) scale(${s}) translate(${-c.x - W / 2},${-c.y - H / 2})` });
      });
    },
  };
}
