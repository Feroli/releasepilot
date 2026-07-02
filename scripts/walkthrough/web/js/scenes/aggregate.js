import { svg, set } from "../dom.js";
import { node, edge, drawOn, chip, text } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutBack } from "../ease.js";

const RESP = [
  { str: "valid state transitions", color: "#22d3ee", x: 300, y: 330 },
  { str: "acting user + timestamps", color: "#8b5cf6", x: 1330, y: 330 },
  { str: "emits domain events", color: "#a3e635", x: 300, y: 730 },
  { str: "terminal immutability", color: "#ec4899", x: 1330, y: 730 },
];

export function aggregate() {
  let head, core, chips, edges, notOwned;
  return {
    id: "aggregate",
    dur: 11,
    build(layer) {
      head = heading(layer, { kicker: "The core decision", title: "Promotion is the aggregate" });

      const edgeLayer = svg("g");
      layer.appendChild(edgeLayer);

      core = node(layer, { x: 720, y: 450, w: 480, h: 170, title: "Promotion", sub: "one version → one step → one target", accent: "#8b5cf6", titleSize: 46, subSize: 22, glow: "glow-strong" });

      chips = [];
      edges = [];
      for (const rr of RESP) {
        const cp = chip(layer, { x: rr.x, y: rr.y, str: rr.str, color: rr.color, size: 22 });
        chips.push(cp);
        const chipCenter = { x: rr.x + (rr.str.length * 22 * 0.62 + 34) / 2, y: rr.y + 20 };
        const anchor = { x: rr.x < 960 ? 720 : 1200, y: rr.y < 540 ? 470 : 600 };
        edges.push(edge(edgeLayer, { from: chipCenter, to: anchor, color: rr.color, width: 2.5, bias: 0.4, glow: "glow-soft" }));
      }

      notOwned = text(layer, { x: 960, y: 862, str: "not HTTP mapping · not DB queries · not queue publishing", size: 22, weight: 500, fill: "#5b6088", anchor: "middle", opacity: 0 });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      core.setPop(clamp((lt - 0.8) / 0.9));
      // gentle breathing glow on the core stroke
      set(core.rect, { "stroke-width": 2 + 1.4 * (0.5 + 0.5 * Math.sin(lt * 2)) });
      chips.forEach((cp, i) => {
        const p = clamp((lt - 2.0 - i * 0.5) / 0.6);
        set(cp.g, { opacity: clamp(p * 1.3), transform: `translate(0,${(1 - easeOutBack(p)) * 16})` });
        drawOn(edges[i], (lt - 2.3 - i * 0.5) / 0.6);
      });
      set(notOwned, { opacity: clamp((lt - 6.5) / 0.8) });
    },
  };
}
