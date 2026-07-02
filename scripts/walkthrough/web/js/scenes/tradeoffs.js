import { svg, set } from "../dom.js";
import { panel, text } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutBack } from "../ease.js";

const CARDS = [
  { x: 150, y: 326, title: "Reliable events", chosen: "transactional outbox", alt: "direct queue publish", reason: "no events lost after commit", color: "#a3e635" },
  { x: 1000, y: 326, title: "CQRS", chosen: "handlers + read DTOs", alt: "full event sourcing", reason: "separation without the overhead", color: "#22d3ee" },
  { x: 150, y: 556, title: "Pipeline", chosen: "fixed dev → staging → prod", alt: "configurable graph", reason: "future work, not challenge scope", color: "#8b5cf6" },
  { x: 1000, y: 556, title: "Integrations", chosen: "stub adapters behind ports", alt: "real HTTP clients", reason: "replaceable; core stays pure", color: "#ec4899" },
];
const W = 770;
const H = 206;

export function tradeoffs() {
  let head, cards;
  return {
    id: "tradeoffs",
    dur: 12,
    build(layer) {
      head = heading(layer, { kicker: "Judgement", title: "Deliberate trade-offs" });
      cards = CARDS.map((c) => {
        const g = svg("g", { opacity: 0 });
        panel(g, { x: c.x, y: c.y, w: W, h: H, rx: 18, fill: "rgba(18,14,44,0.85)", stroke: c.color, strokeW: 1.6, glow: "glow-soft" });
        text(g, { x: c.x + 34, y: c.y + 52, str: c.title, size: 30, weight: 750, fill: c.color });
        text(g, { x: c.x + 34, y: c.y + 104, str: `✓  ${c.chosen}`, size: 26, weight: 650, fill: "#f5f7ff" });
        text(g, { x: c.x + 34, y: c.y + 142, str: `✕  ${c.alt}`, size: 22, weight: 500, fill: "#5b6088" });
        text(g, { x: c.x + 34, y: c.y + 180, str: c.reason, size: 20, weight: 500, fill: "#9aa3c7" });
        layer.appendChild(g);
        return { g, x: c.x, y: c.y };
      });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      cards.forEach((c, i) => {
        const p = clamp((lt - 0.8 - i * 0.55) / 0.6);
        const s = 0.9 + 0.1 * easeOutBack(p);
        set(c.g, { opacity: clamp(p * 1.3), transform: `translate(${c.x + W / 2},${c.y + H / 2}) scale(${s}) translate(${-c.x - W / 2},${-c.y - H / 2})` });
      });
    },
  };
}
