import { svg, set } from "../dom.js";
import { panel, text, codeBlock, pill } from "../kit.js";
import { clamp, easeOutBack, easeOutCubic, lerp } from "../ease.js";

const CHIPS = [
  { s: "DDD", w: 110, c: "#22d3ee" },
  { s: "CQRS", w: 130, c: "#8b5cf6" },
  { s: "Transactional Outbox", w: 320, c: "#fbbf24" },
  { s: "Hexagonal Ports", w: 260, c: "#a3e635" },
  { s: "Testcontainers", w: 240, c: "#ec4899" },
];
const GAP = 26;

export function outro() {
  let title, card, code, chips, thanks;
  return {
    id: "outro",
    dur: 10,
    build(layer) {
      title = text(layer, { x: 960, y: 340, str: "ReleasePilot", size: 100, weight: 800, anchor: "middle", spacing: "-1.5px" });
      set(title, { filter: "url(#glow-strong)", opacity: 0 });

      card = svg("g", { opacity: 0 });
      panel(card, { x: 560, y: 410, w: 800, h: 190, rx: 18, fill: "rgba(6,5,20,0.96)", stroke: "rgba(34,211,238,0.5)", glow: "glow-soft" });
      panel(card, { x: 560, y: 410, w: 800, h: 40, rx: 18, fill: "rgba(139,129,216,0.14)" });
      [["#f87171", 588], ["#fbbf24", 610], ["#a3e635", 632]].forEach(([c, cx]) => card.appendChild(svg("circle", { cx, cy: 430, r: 7, fill: c })));
      text(card, { x: 1320, y: 436, str: "run locally", size: 16, weight: 600, fill: "#9aa3c7", family: "var(--mono)", anchor: "end" });
      layer.appendChild(card);
      code = codeBlock(layer, {
        x: 590,
        y: 500,
        size: 25,
        gap: 46,
        lines: ["$ docker compose up -d      # Postgres + RabbitMQ", "$ ./gradlew bootRun         # http://localhost:8080"],
        colorFn: () => "#a3e635",
      });

      const total = CHIPS.reduce((a, c) => a + c.w, 0) + GAP * (CHIPS.length - 1);
      let x = 960 - total / 2;
      chips = CHIPS.map((c) => {
        const p = pill(layer, { x, y: 672, w: c.w, h: 52, str: c.s, color: c.c, size: 21 });
        x += c.w + GAP;
        return p;
      });

      thanks = text(layer, { x: 960, y: 800, str: "thanks for watching ✦", size: 30, weight: 600, fill: "#9aa3c7", anchor: "middle", opacity: 0 });
    },
    render(lt) {
      const pop = easeOutBack(clamp((lt - 0.2) / 1.0));
      set(title, { opacity: clamp((lt - 0.2) / 0.7), transform: `translate(960,340) scale(${lerp(0.85, 1, pop)}) translate(-960,-340)` });
      set(card, { opacity: easeOutCubic(clamp((lt - 1.2) / 0.6)) });
      code.setReveal((lt - 1.7) * 40);
      chips.forEach((c, i) => {
        const p = clamp((lt - 3.6 - i * 0.22) / 0.5);
        set(c.g, { opacity: clamp(p * 1.3), transform: `translate(0,${(1 - easeOutBack(p)) * 18})` });
      });
      set(thanks, { opacity: clamp((lt - 5.6) / 0.8) });
    },
  };
}
