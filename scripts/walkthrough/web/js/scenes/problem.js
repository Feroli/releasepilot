import { svg, set } from "../dom.js";
import { drawOn, text, pill } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutBack, smoothstep } from "../ease.js";

const ENVS = [
  { label: "DEV", color: "#22d3ee", x: 500 },
  { label: "STAGING", color: "#8b5cf6", x: 960 },
  { label: "PROD", color: "#a3e635", x: 1420 },
];
const CY = 470;
const R = 52;

export function problem() {
  let head, dots, labels, okPath, okArrow, okMark, badPath, badArrow, badMark, pills;
  return {
    id: "problem",
    dur: 10,
    build(layer) {
      head = heading(layer, { kicker: "The problem", title: "Move versions, one safe step at a time" });

      dots = [];
      labels = [];
      for (const env of ENVS) {
        const c = svg("circle", { cx: env.x, cy: CY, r: R, fill: "url(#grad-panel)", stroke: env.color, "stroke-width": 3, filter: "url(#glow-soft)", opacity: 0 });
        layer.appendChild(c);
        dots.push(c);
        labels.push(text(layer, { x: env.x, y: CY + 96, str: env.label, size: 22, weight: 700, fill: env.color, anchor: "middle", opacity: 0 }));
      }

      const el = svg("g");
      layer.appendChild(el);

      // allowed: DEV -> STAGING, bows UP above the circles
      okPath = svg("path", { d: `M ${ENVS[0].x + R} ${CY - 24} C ${ENVS[0].x + 150} ${CY - 92}, ${ENVS[1].x - 150} ${CY - 92}, ${ENVS[1].x - R} ${CY - 24}`, fill: "none", stroke: "#a3e635", "stroke-width": 4, "stroke-linecap": "round", filter: "url(#glow-soft)" });
      el.appendChild(okPath);
      okArrow = svg("polygon", { points: `${ENVS[1].x - R},${CY - 24} ${ENVS[1].x - R - 16},${CY - 34} ${ENVS[1].x - R - 8},${CY - 12}`, fill: "#a3e635", opacity: 0 });
      el.appendChild(okArrow);
      okMark = text(el, { x: (ENVS[0].x + ENVS[1].x) / 2, y: CY - 104, str: "✓ allowed", size: 24, weight: 700, fill: "#a3e635", anchor: "middle", opacity: 0 });

      // blocked: DEV -> PROD, bows DOWN well below the labels
      badPath = svg("path", { d: `M ${ENVS[0].x} ${CY + R} C ${ENVS[0].x + 220} ${CY + 250}, ${ENVS[2].x - 220} ${CY + 250}, ${ENVS[2].x} ${CY + R}`, fill: "none", stroke: "#f87171", "stroke-width": 4, "stroke-linecap": "round", "stroke-dasharray": "11 9" });
      el.appendChild(badPath);
      badArrow = svg("polygon", { points: `${ENVS[2].x},${CY + R} ${ENVS[2].x + 4},${CY + R + 20} ${ENVS[2].x + 20},${CY + R + 4}`, fill: "#f87171", opacity: 0 });
      el.appendChild(badArrow);
      badMark = text(el, { x: 960, y: CY + 258, str: "✕ no skipping to production", size: 24, weight: 700, fill: "#f87171", anchor: "middle", opacity: 0 });

      pills = [
        pill(layer, { x: 520, y: 812, w: 250, str: "Approved", color: "#22d3ee" }),
        pill(layer, { x: 835, y: 812, w: 250, str: "Tracked", color: "#8b5cf6" }),
        pill(layer, { x: 1150, y: 812, w: 250, str: "Reversible", color: "#a3e635" }),
      ];
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      ENVS.forEach((_, i) => {
        const p = clamp((lt - 0.8 - i * 0.25) / 0.6);
        set(dots[i], { opacity: clamp(p * 1.3), r: R * (0.7 + 0.3 * easeOutBack(p)) });
        set(labels[i], { opacity: clamp((lt - 1.0 - i * 0.25) / 0.6) });
      });
      drawOn(okPath, (lt - 2.0) / 0.6);
      set(okArrow, { opacity: clamp((lt - 2.5) / 0.4) });
      set(okMark, { opacity: clamp((lt - 2.5) / 0.5) });
      drawOn(badPath, (lt - 3.4) / 0.7);
      const bx = smoothstep(4.0, 4.5, lt);
      set(badArrow, { opacity: bx });
      set(badMark, { opacity: bx });
      pills.forEach((pl, i) => {
        const p = clamp((lt - 5.2 - i * 0.4) / 0.6);
        set(pl.g, { opacity: clamp(p * 1.3), transform: `translate(0,${(1 - easeOutBack(p)) * 18})` });
      });
    },
  };
}
