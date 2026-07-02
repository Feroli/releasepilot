import { svg, set } from "../dom.js";
import { node, edge, drawOn, packet, placeOnPath, text, pill } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeInOutCubic, easeOutCubic, smoothstep } from "../ease.js";

function dbCyl(parent, cx, cy, color) {
  const g = svg("g", { opacity: 0 });
  const rx = 78;
  const ry = 18;
  const h = 84;
  g.appendChild(svg("path", { d: `M ${cx - rx} ${cy - h / 2} A ${rx} ${ry} 0 0 0 ${cx + rx} ${cy - h / 2} L ${cx + rx} ${cy + h / 2} A ${rx} ${ry} 0 0 1 ${cx - rx} ${cy + h / 2} Z`, fill: "url(#grad-panel)", stroke: color, "stroke-width": 2.5, filter: "url(#glow-soft)" }));
  g.appendChild(svg("ellipse", { cx, cy: cy - h / 2, rx, ry, fill: "rgba(163,230,53,0.14)", stroke: color, "stroke-width": 2.5 }));
  text(g, { x: cx, y: cy + 6, str: "audit_log", size: 18, weight: 700, fill: color, anchor: "middle", family: "var(--mono)" });
  parent.appendChild(g);
  return g;
}

export function asyncFlow() {
  let head, client, api, outbox, pub, rabbit, audit, notify, db;
  let respEdge, persistEdge, chain, notifyEdge, respLbl, asyncPill, p1, p2, pr;
  return {
    id: "asyncFlow",
    dur: 13,
    build(layer) {
      head = heading(layer, { kicker: "Async handoff", title: "Respond first, process after" });
      const el = svg("g");
      layer.appendChild(el);

      client = pill(layer, { x: 470, y: 300, w: 220, h: 60, str: "API Client", color: "#22d3ee", size: 22 });
      api = node(layer, { x: 140, y: 400, w: 300, h: 100, title: "API Transaction", accent: "#8b5cf6", titleSize: 23 });
      outbox = node(layer, { x: 140, y: 588, w: 300, h: 100, title: "outbox_events", sub: "persisted in same txn", accent: "#fbbf24", titleSize: 22, subSize: 15 });
      pub = node(layer, { x: 560, y: 588, w: 270, h: 100, title: "Outbox Publisher", accent: "#8b5cf6", titleSize: 21 });
      rabbit = node(layer, { x: 950, y: 588, w: 250, h: 100, title: "RabbitMQ", sub: "topic exchange", accent: "#ec4899", titleSize: 24, subSize: 15 });
      audit = node(layer, { x: 1320, y: 430, w: 300, h: 96, title: "Audit Consumer", accent: "#a3e635", titleSize: 22 });
      notify = node(layer, { x: 1320, y: 690, w: 300, h: 96, title: "Notification Consumer", accent: "#22d3ee", titleSize: 20 });
      db = dbCyl(layer, 1770, 478, "#a3e635");

      respEdge = edge(el, { from: api.right, to: { x: 470, y: 340 }, color: "#a3e635", width: 3, bias: 0.4, arrow: true, glow: "glow-soft" });
      respLbl = text(layer, { x: 520, y: 392, str: "200 OK — returns now", size: 18, weight: 700, fill: "#a3e635", opacity: 0 });
      persistEdge = edge(el, { from: api.bottom, to: outbox.top, color: "#fbbf24", width: 2.5, bias: 1, arrow: true });

      const e1 = edge(el, { from: outbox.right, to: pub.left, color: "#8b5cf6", width: 2.5, bias: 0, arrow: true });
      const e2 = edge(el, { from: pub.right, to: rabbit.left, color: "#ec4899", width: 2.5, bias: 0, arrow: true });
      const e3 = edge(el, { from: rabbit.right, to: audit.left, color: "#a3e635", width: 2.5, bias: 0.4, arrow: true });
      const e4 = edge(el, { from: audit.right, to: { x: 1692, y: 478 }, color: "#a3e635", width: 2.5, bias: 0.3, arrow: true });
      notifyEdge = edge(el, { from: rabbit.right, to: notify.left, color: "#22d3ee", width: 2.5, bias: 0.4, arrow: true });
      chain = [e1, e2, e3, e4];

      asyncPill = pill(layer, { x: 1360, y: 578, w: 220, h: 44, str: "non-blocking", color: "#a3e635", size: 18 });

      p1 = packet(layer, { r: 8, color: "#ffffff", glow: "glow-strong" });
      p2 = packet(layer, { r: 7, color: "#ec4899", glow: "glow-strong" });
      pr = packet(layer, { r: 8, color: "#a3e635", glow: "glow-strong" });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      api.setPop(clamp((lt - 0.7) / 0.6));
      set(client.g, { opacity: clamp((lt - 1.0) / 0.6) });
      drawOn(respEdge, (lt - 1.4) / 0.5);
      set(respLbl, { opacity: clamp((lt - 1.7) / 0.5) });
      // early response packet
      const rp = (lt - 1.5) / 0.7;
      set(pr, { opacity: rp > 0 && rp < 1 ? 1 : 0 });
      if (rp > 0 && rp < 1) {
        placeOnPath(pr, respEdge, easeInOutCubic(rp));
      }

      outbox.setPop(clamp((lt - 2.4) / 0.6));
      drawOn(persistEdge, (lt - 2.1) / 0.5);
      pub.setPop(clamp((lt - 3.2) / 0.6));
      rabbit.setPop(clamp((lt - 3.9) / 0.6));
      audit.setPop(clamp((lt - 4.6) / 0.6));
      notify.setPop(clamp((lt - 5.0) / 0.6));
      set(db, { opacity: clamp((lt - 5.4) / 0.6) });
      drawOn(chain[0], (lt - 3.4) / 0.4);
      drawOn(chain[1], (lt - 4.0) / 0.4);
      drawOn(chain[2], (lt - 4.7) / 0.4);
      drawOn(chain[3], (lt - 5.3) / 0.4);
      drawOn(notifyEdge, (lt - 5.1) / 0.4);
      set(asyncPill.g, { opacity: clamp((lt - 6.0) / 0.6) });

      // looping async packets along the chain
      const flow = (t0, node) => {
        const p = ((lt - t0) / 4.2) % 1;
        if (lt < t0) {
          set(node, { opacity: 0 });
          return;
        }
        set(node, { opacity: smoothstep(0, 0.05, p) * (1 - smoothstep(0.95, 1, p)) });
        const seg = p * chain.length;
        const idx = Math.min(chain.length - 1, Math.floor(seg));
        placeOnPath(node, chain[idx], easeInOutCubic(seg - idx));
      };
      flow(6.2, p1);
      flow(8.4, p2);
    },
  };
}
