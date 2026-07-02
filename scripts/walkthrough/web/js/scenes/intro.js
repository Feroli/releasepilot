import { svg, set } from "../dom.js";
import { text, edge, packet, placeOnPath } from "../kit.js";
import { clamp, lerp, easeOutBack, easeInOutCubic, mapRange, smoothstep } from "../ease.js";

const ENVS = [
  { label: "DEV", color: "#22d3ee", x: 470 },
  { label: "STAGING", color: "#8b5cf6", x: 910 },
  { label: "PROD", color: "#a3e635", x: 1350 },
];
const PY = 650;
const PW = 260;
const PH = 96;

export function intro() {
  let title, tagline, boxes, rects, edges, dot;
  return {
    id: "intro",
    dur: 9,
    build(layer) {
      title = text(layer, { x: 960, y: 415, str: "ReleasePilot", size: 132, weight: 800, anchor: "middle", spacing: "-2px" });
      set(title, { filter: "url(#glow-strong)", opacity: 0 });
      tagline = text(layer, { x: 960, y: 485, str: "the deployment promotion engine", size: 30, weight: 500, fill: "#9aa3c7", anchor: "middle", opacity: 0 });

      const edgeLayer = svg("g");
      layer.appendChild(edgeLayer);
      edges = [];
      for (let i = 0; i < ENVS.length - 1; i++) {
        edges.push(
          edge(edgeLayer, {
            from: { x: ENVS[i].x + PW, y: PY + PH / 2 },
            to: { x: ENVS[i + 1].x, y: PY + PH / 2 },
            color: "url(#grad-beam)",
            width: 5,
            bias: 0,
            glow: "glow-soft",
          })
        );
      }

      boxes = [];
      rects = [];
      for (const env of ENVS) {
        const g = svg("g", { opacity: 0 });
        const r = svg("rect", { x: env.x, y: PY, width: PW, height: PH, rx: 20, fill: "url(#grad-panel)", stroke: env.color, "stroke-width": 2.5, filter: "url(#glow-soft)" });
        g.appendChild(r);
        text(g, { x: env.x + PW / 2, y: PY + PH / 2 + 12, str: env.label, size: 34, weight: 750, fill: env.color, anchor: "middle" });
        layer.appendChild(g);
        boxes.push(g);
        rects.push(r);
      }

      // full path for the travelling packet, spanning the whole pipeline
      dot = packet(layer, { r: 10, color: "#ffffff", glow: "glow-huge" });
    },
    render(lt) {
      const pop = easeOutBack(clamp((lt - 0.2) / 1.3));
      set(title, { opacity: clamp((lt - 0.2) / 0.8), transform: `translate(960,415) scale(${lerp(0.8, 1, pop)}) translate(-960,-415)` });
      set(tagline, { opacity: clamp((lt - 1.1) / 0.7) });

      ENVS.forEach((env, i) => {
        const p = clamp((lt - 1.7 - i * 0.4) / 0.7);
        set(boxes[i], { opacity: clamp(p * 1.3), transform: `translate(0,${(1 - easeOutBack(p)) * 20})` });
        // light-up pulse as the packet passes
        const passT = 3.4 + i * 1.3;
        const pulse = smoothstep(passT - 0.4, passT, lt) * (1 - smoothstep(passT + 0.5, passT + 1.1, lt));
        set(rects[i], { "stroke-width": 2.5 + pulse * 5 });
      });

      edges.forEach((e, i) => {
        const p = clamp((lt - 2.2 - i * 0.5) / 0.6);
        const len = e.getTotalLength();
        set(e, { "stroke-dasharray": len, "stroke-dashoffset": len * (1 - p) });
      });

      // packet crosses the whole pipeline
      const pk = clamp((lt - 3.2) / 4.2);
      if (pk > 0 && pk < 1) {
        set(dot, { opacity: 1 });
        const seg = pk * (ENVS.length - 1);
        const idx = Math.min(edges.length - 1, Math.floor(seg));
        placeOnPath(dot, edges[idx], easeInOutCubic(seg - idx));
      } else {
        set(dot, { opacity: 0 });
      }
    },
  };
}
