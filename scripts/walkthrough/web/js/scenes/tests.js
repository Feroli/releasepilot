import { svg, set } from "../dom.js";
import { text, edge, drawOn } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutBack, easeOutCubic } from "../ease.js";

const CX = 600;
const APEX = 320;
const BASE = 772;
const HW = 330;
const hw = (y) => (HW * (y - APEX)) / (BASE - APEX);

const TIERS = [
  { y1: 320, y2: 470, color: "#a3e635", label: "Integration & E2E", detail: "Testcontainers: Postgres, RabbitMQ, async audit" },
  { y1: 470, y2: 620, color: "#8b5cf6", label: "API tests", detail: "MockMvc: commands, 4xx errors, read models" },
  { y1: 620, y2: 772, color: "#22d3ee", label: "Domain unit tests", detail: "aggregate & pipeline invariants" },
];

export function tests() {
  let head, bands, labels, lines, check;
  return {
    id: "tests",
    dur: 10,
    build(layer) {
      head = heading(layer, { kicker: "Confidence", title: "Tested top to bottom" });
      const el = svg("g");
      layer.appendChild(el);

      bands = [];
      labels = [];
      lines = [];
      TIERS.forEach((t, i) => {
        const pts =
          i === 0
            ? `${CX},${t.y1} ${CX + hw(t.y2)},${t.y2} ${CX - hw(t.y2)},${t.y2}`
            : `${CX - hw(t.y1)},${t.y1} ${CX + hw(t.y1)},${t.y1} ${CX + hw(t.y2)},${t.y2} ${CX - hw(t.y2)},${t.y2}`;
        const poly = svg("polygon", { points: pts, fill: t.color, "fill-opacity": 0.16, stroke: t.color, "stroke-width": 2.5, filter: "url(#glow-soft)", opacity: 0 });
        layer.appendChild(poly);
        bands.push(poly);

        const midY = (t.y1 + t.y2) / 2 + 8;
        const lg = svg("g", { opacity: 0 });
        text(lg, { x: 1080, y: midY - 12, str: t.label, size: 30, weight: 700, fill: t.color });
        text(lg, { x: 1080, y: midY + 22, str: t.detail, size: 21, weight: 500, fill: "#9aa3c7", family: "var(--mono)" });
        layer.appendChild(lg);
        labels.push(lg);
        lines.push(edge(el, { from: { x: CX + hw(t.y2) - 10, y: midY - 6 }, to: { x: 1060, y: midY - 6 }, color: t.color, width: 2, bias: 0.1 }));
      });

      check = svg("g", { opacity: 0 });
      text(check, { x: CX, y: 862, str: "✓ green build — every layer passing", size: 26, weight: 700, fill: "#a3e635", anchor: "middle" });
      layer.appendChild(check);
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      // reveal base first (bottom up)
      TIERS.forEach((t, i) => {
        const order = TIERS.length - 1 - i; // base = 0
        const p = clamp((lt - 0.8 - order * 0.6) / 0.6);
        set(bands[i], { opacity: easeOutCubic(p), transform: `translate(0,${(1 - easeOutBack(p)) * 24})` });
        set(labels[i], { opacity: clamp((lt - 1.1 - order * 0.6) / 0.6) });
        drawOn(lines[i], (lt - 1.0 - order * 0.6) / 0.4);
      });
      set(check, { opacity: clamp((lt - 4.4) / 0.7) });
    },
  };
}
