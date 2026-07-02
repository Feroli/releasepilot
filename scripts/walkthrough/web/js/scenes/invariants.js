import { svg, set } from "../dom.js";
import { text, panel } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutCubic, easeOutBack } from "../ease.js";

const RULES = [
  { n: "1", str: "Move exactly one step — never skip", mech: "aggregate guard", color: "#22d3ee" },
  { n: "2", str: "Previous environment completed first", mech: "eligibility snapshot", color: "#8b5cf6" },
  { n: "3", str: "One active promotion per app + target", mech: "partial unique index", color: "#a3e635" },
  { n: "4", str: "Only approvers can approve", mech: "ApproverDirectory", color: "#fbbf24" },
  { n: "5", str: "Terminal states are immutable", mech: "terminal_at guard", color: "#ec4899" },
];
const X = 330;
const W = 1260;

export function invariants() {
  let head, rows;
  return {
    id: "invariants",
    dur: 12,
    build(layer) {
      head = heading(layer, { kicker: "Where the rules live", title: "Invariants inside the aggregate" });
      rows = RULES.map((r, i) => {
        const y = 330 + i * 102;
        const g = svg("g", { opacity: 0 });
        panel(g, { x: X, y, w: W, h: 82, rx: 16, fill: "rgba(20,18,48,0.55)", stroke: "rgba(139,129,216,0.22)" });
        const badge = svg("circle", { cx: X + 52, cy: y + 41, r: 26, fill: r.color, "fill-opacity": 0.16, stroke: r.color, "stroke-width": 2, filter: "url(#glow-soft)" });
        g.appendChild(badge);
        text(g, { x: X + 52, y: y + 50, str: r.n, size: 26, weight: 750, fill: r.color, anchor: "middle" });
        text(g, { x: X + 108, y: y + 51, str: r.str, size: 28, weight: 600, fill: "#f5f7ff" });
        text(g, { x: X + W - 30, y: y + 51, str: r.mech, size: 20, weight: 500, fill: "#5b6088", anchor: "end", family: "var(--mono)" });
        layer.appendChild(g);
        return { g, badge };
      });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      rows.forEach((row, i) => {
        const p = clamp((lt - 0.9 - i * 0.55) / 0.6);
        set(row.g, { opacity: clamp(p * 1.3), transform: `translate(${(1 - easeOutCubic(p)) * -46},0)` });
        set(row.badge, { r: 26 * (0.6 + 0.4 * easeOutBack(p)) });
      });
    },
  };
}
