import { svg, set } from "../dom.js";
import { node, edge, drawOn, packet, placeOnPath, text } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeInOutCubic, smoothstep } from "../ease.js";

const NY = 350;
const NW = 250;
const NH = 108;
const STATES = [
  { label: "REQUESTED", color: "#22d3ee", x: 170 },
  { label: "APPROVED", color: "#8b5cf6", x: 610 },
  { label: "DEPLOYING", color: "#fbbf24", x: 1050 },
  { label: "COMPLETED", color: "#a3e635", x: 1490 },
];
const CMDS = ["request", "approve", "startDeployment", "complete"];

export function stateMachine() {
  let head, nodes, edges, cmdLabels, token, cancelled, rolledBack, bCancel, bRoll, bCancelLbl, bRollLbl;
  return {
    id: "stateMachine",
    dur: 15,
    build(layer) {
      head = heading(layer, { kicker: "Lifecycle", title: "A strict state machine" });

      const edgeLayer = svg("g");
      layer.appendChild(edgeLayer);

      nodes = STATES.map((s) => node(layer, { x: s.x, y: NY, w: NW, h: NH, title: s.label, accent: s.color, titleSize: 25, glow: "glow-soft" }));

      edges = [];
      cmdLabels = [];
      for (let i = 0; i < STATES.length - 1; i++) {
        edges.push(edge(edgeLayer, { from: { x: STATES[i].x + NW, y: NY + NH / 2 }, to: { x: STATES[i + 1].x, y: NY + NH / 2 }, color: "url(#grad-beam)", width: 3.5, bias: 0, arrow: true, glow: "glow-soft" }));
        const mid = (STATES[i].x + NW + STATES[i + 1].x) / 2;
        cmdLabels.push(text(layer, { x: mid, y: NY - 22, str: CMDS[i], size: 20, weight: 600, fill: "#9aa3c7", anchor: "middle", family: "var(--mono)", opacity: 0 }));
      }
      // first command label above REQUESTED
      cmdLabels.push(text(layer, { x: STATES[0].x + NW / 2, y: NY - 22, str: CMDS[0], size: 20, weight: 600, fill: "#9aa3c7", anchor: "middle", family: "var(--mono)", opacity: 0 }));

      token = packet(layer, { r: 12, color: "#ffffff", glow: "glow-huge" });

      cancelled = node(layer, { x: 610, y: 640, w: 250, h: 96, title: "CANCELLED", accent: "#f87171", titleSize: 23, glow: "glow-soft" });
      rolledBack = node(layer, { x: 1050, y: 640, w: 250, h: 96, title: "ROLLED_BACK", accent: "#ec4899", titleSize: 23, glow: "glow-soft" });

      bCancel = edge(edgeLayer, { from: { x: 735, y: NY + NH }, to: { x: 735, y: 640 }, color: "#f87171", width: 3, bias: 1, dash: "9 8" });
      bRoll = edge(edgeLayer, { from: { x: 1175, y: NY + NH }, to: { x: 1175, y: 640 }, color: "#ec4899", width: 3, bias: 1, dash: "9 8" });
      bCancelLbl = text(layer, { x: 470, y: 700, str: "cancel — any live state", size: 19, weight: 600, fill: "#f87171", anchor: "end", opacity: 0 });
      bRollLbl = text(layer, { x: 1330, y: 700, str: "rollback — approved / deploying", size: 19, weight: 600, fill: "#ec4899", anchor: "start", opacity: 0 });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      nodes.forEach((n, i) => n.setPop(clamp((lt - 0.7 - i * 0.3) / 0.6)));
      set(cmdLabels[3], { opacity: clamp((lt - 0.9) / 0.5) }); // 'request' above first
      edges.forEach((e, i) => drawOn(e, (lt - 1.6 - i * 0.35) / 0.6));

      // happy-path token walk
      const walk = clamp((lt - 2.6) / 5.6);
      if (walk > 0 && walk <= 1) {
        set(token, { opacity: 1 });
        const seg = walk * edges.length;
        const idx = Math.min(edges.length - 1, Math.floor(seg));
        placeOnPath(token, edges[idx], easeInOutCubic(seg - idx));
        // reveal moving command label
        set(cmdLabels[idx], { opacity: 1 });
      } else if (walk > 1) {
        set(token, { opacity: 0 });
      } else {
        set(token, { opacity: 0 });
      }

      // light nodes as the token reaches them
      nodes.forEach((n, i) => {
        const arriveAt = 2.6 + (i / edges.length) * 5.6;
        const lit = smoothstep(arriveAt - 0.3, arriveAt + 0.2, lt);
        set(n.rect, { "stroke-width": 2 + lit * 4 });
      });

      // terminal branches
      cancelled.setPop(clamp((lt - 9.4) / 0.7));
      rolledBack.setPop(clamp((lt - 10.0) / 0.7));
      drawOn(bCancel, (lt - 9.7) / 0.6);
      drawOn(bRoll, (lt - 10.3) / 0.6);
      set(bCancelLbl, { opacity: clamp((lt - 10.2) / 0.6) });
      set(bRollLbl, { opacity: clamp((lt - 10.8) / 0.6) });
    },
  };
}
