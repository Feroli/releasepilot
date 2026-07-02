import { svg, set } from "../dom.js";
import { text, edge, drawOn, pill } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutCubic } from "../ease.js";

const ROWS = [
  { err: "EnvironmentSkipped", code: "409 · Conflict", color: "#fbbf24" },
  { err: "PreviousEnvironmentIncomplete", code: "409 · Conflict", color: "#fbbf24" },
  { err: "PromotionAlreadyInProgress", code: "409 · Conflict", color: "#fbbf24" },
  { err: "InvalidPromotionState", code: "409 · Conflict", color: "#fbbf24" },
  { err: "ApproverRequired", code: "403 · Forbidden", color: "#ec4899" },
  { err: "PromotionImmutable", code: "409 · Conflict", color: "#fbbf24" },
  { err: "ResourceNotFound", code: "404 · Not Found", color: "#22d3ee" },
];

export function errors() {
  let head, rows, note;
  return {
    id: "errors",
    dur: 10,
    build(layer) {
      head = heading(layer, { kicker: "Failure is explicit", title: "Domain errors → HTTP status" });
      rows = ROWS.map((r, i) => {
        const y = 330 + i * 70;
        const g = svg("g", { opacity: 0 });
        text(g, { x: 320, y: y + 6, str: r.err, size: 27, weight: 600, fill: "#f5f7ff", family: "var(--mono)" });
        const e = edge(g, { from: { x: 1000, y: y - 4 }, to: { x: 1160, y: y - 4 }, color: r.color, width: 2.5, bias: 0, arrow: true });
        const p = pill(g, { x: 1200, y: y - 30, w: 320, h: 52, str: r.code, color: r.color, size: 24 });
        set(p.g, { opacity: 1 });
        layer.appendChild(g);
        return { g, e };
      });
      note = text(layer, { x: 320, y: 862, str: "returned as RFC-7807 Problem Details", size: 20, weight: 500, fill: "#5b6088", family: "var(--mono)", opacity: 0 });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      rows.forEach((row, i) => {
        const p = clamp((lt - 0.8 - i * 0.4) / 0.6);
        set(row.g, { opacity: clamp(p * 1.3), transform: `translate(${(1 - easeOutCubic(p)) * -40},0)` });
        drawOn(row.e, (lt - 1.0 - i * 0.4) / 0.4);
      });
      set(note, { opacity: clamp((lt - 4.5) / 0.8) });
    },
  };
}
