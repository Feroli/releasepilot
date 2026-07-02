import { svg, set } from "../dom.js";
import { edge, drawOn, packet, placeOnPath, text, panel } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeInOutCubic, easeOutCubic } from "../ease.js";

const LIFELINES = [
  { name: "Client", x: 240 },
  { name: "Controller", x: 560 },
  { name: "Handler", x: 920 },
  { name: "Aggregate", x: 1300 },
  { name: "DB + Outbox", x: 1660 },
];
const TOP = 350;
const BOT = 838;

const MSGS = [
  { from: 0, to: 1, y: 406, label: "POST /promotions", color: "#22d3ee", at: 1.4 },
  { from: 1, to: 2, y: 470, label: "RequestPromotion", color: "#8b5cf6", at: 2.7 },
  { from: 2, to: 3, y: 534, label: "Promotion.request(snapshot)", color: "#8b5cf6", at: 4.0 },
  { from: 3, to: 2, y: 592, label: "aggregate + PromotionRequested", color: "#a3e635", dash: "8 7", at: 5.3 },
  { from: 2, to: 4, y: 680, label: "save aggregate + outbox event", color: "#fbbf24", at: 7.2 },
  { from: 2, to: 1, y: 760, label: "response DTO", color: "#9aa3c7", dash: "8 7", at: 9.4 },
  { from: 1, to: 0, y: 812, label: "201 Created", color: "#a3e635", dash: "8 7", at: 10.7 },
];

export function commandFlow() {
  let head, headers, lifelines, txn, txnLbl, msgs, dot;
  return {
    id: "commandFlow",
    dur: 14,
    build(layer) {
      head = heading(layer, { kicker: "Write path", title: "One command, one transaction" });

      const edgeLayer = svg("g");
      layer.appendChild(edgeLayer);

      lifelines = [];
      headers = [];
      for (const ll of LIFELINES) {
        const h = svg("g", { opacity: 0 });
        panel(h, { x: ll.x - 90, y: TOP - 46, w: 180, h: 52, rx: 12, fill: "rgba(20,18,48,0.8)", stroke: "rgba(139,129,216,0.4)" });
        text(h, { x: ll.x, y: TOP - 12, str: ll.name, size: 20, weight: 700, fill: "#f5f7ff", anchor: "middle" });
        layer.appendChild(h);
        headers.push(h);
        const line = svg("line", { x1: ll.x, y1: TOP, x2: ll.x, y2: BOT, stroke: "rgba(139,129,216,0.28)", "stroke-width": 2, "stroke-dasharray": "4 8", opacity: 0 });
        layer.appendChild(line);
        lifelines.push(line);
      }

      // transaction highlight around the save step
      txn = panel(layer, { x: 830, y: 636, w: 900, h: 88, rx: 14, fill: "rgba(251,191,36,0.07)", stroke: "rgba(251,191,36,0.5)", strokeW: 1.6 });
      set(txn, { opacity: 0, "stroke-dasharray": "10 6" });
      txnLbl = text(layer, { x: 852, y: 660, str: "@Transactional", size: 18, weight: 700, fill: "#fbbf24", family: "var(--mono)", opacity: 0 });

      msgs = MSGS.map((m) => {
        const fx = LIFELINES[m.from].x;
        const tx = LIFELINES[m.to].x;
        const path = edge(edgeLayer, { from: { x: fx, y: m.y }, to: { x: tx, y: m.y }, color: m.color, width: 3, bias: 0, arrow: true, dash: m.dash });
        const lbl = text(layer, { x: (fx + tx) / 2, y: m.y - 14, str: m.label, size: 19, weight: 600, fill: m.color, anchor: "middle", family: "var(--mono)", opacity: 0 });
        return { path, lbl, m };
      });

      dot = packet(layer, { r: 8, color: "#ffffff", glow: "glow-strong" });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      LIFELINES.forEach((_, i) => {
        const p = clamp((lt - 0.5 - i * 0.12) / 0.5);
        set(headers[i], { opacity: clamp(p * 1.3), transform: `translate(0,${(1 - easeOutCubic(p)) * -14})` });
        set(lifelines[i], { opacity: easeOutCubic(clamp((lt - 0.7 - i * 0.12) / 0.6)) * 0.9 });
      });

      let activeDot = null;
      msgs.forEach(({ path, lbl, m }) => {
        drawOn(path, (lt - m.at) / 0.45);
        set(lbl, { opacity: clamp((lt - m.at - 0.1) / 0.4) });
        const dp = (lt - m.at) / 0.7;
        if (dp > 0 && dp < 1) {
          activeDot = { path, p: dp };
        }
      });
      if (activeDot) {
        set(dot, { opacity: 1 });
        placeOnPath(dot, activeDot.path, easeInOutCubic(activeDot.p));
      } else {
        set(dot, { opacity: 0 });
      }

      const tp = easeOutCubic(clamp((lt - 6.9) / 0.6));
      set(txn, { opacity: tp });
      set(txnLbl, { opacity: tp });
    },
  };
}
