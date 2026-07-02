import { svg, set } from "../dom.js";
import { node, edge, drawOn, text } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutCubic } from "../ease.js";

function cylinder(parent, cx, cy, w, h, color) {
  const g = svg("g", { opacity: 0 });
  const rx = w / 2;
  const ry = 22;
  g.appendChild(svg("path", { d: `M ${cx - rx} ${cy - h / 2} A ${rx} ${ry} 0 0 0 ${cx + rx} ${cy - h / 2} L ${cx + rx} ${cy + h / 2} A ${rx} ${ry} 0 0 1 ${cx - rx} ${cy + h / 2} Z`, fill: "url(#grad-panel)", stroke: color, "stroke-width": 2.5, filter: "url(#glow-soft)" }));
  g.appendChild(svg("ellipse", { cx, cy: cy - h / 2, rx, ry, fill: "rgba(34,211,238,0.14)", stroke: color, "stroke-width": 2.5 }));
  text(g, { x: cx, y: cy + 8, str: "PostgreSQL", size: 22, weight: 700, fill: color, anchor: "middle" });
  parent.appendChild(g);
  return g;
}

export function cqrs() {
  let head, divider, wLbl, rLbl, left, right, vEdges, dbEdges, db;
  return {
    id: "cqrs",
    dur: 10,
    build(layer) {
      head = heading(layer, { kicker: "Separation", title: "Commands vs Queries" });

      const edgeLayer = svg("g");
      layer.appendChild(edgeLayer);

      divider = svg("line", { x1: 960, y1: 320, x2: 960, y2: 700, stroke: "rgba(139,129,216,0.4)", "stroke-width": 2, "stroke-dasharray": "6 8", opacity: 0 });
      layer.appendChild(divider);
      wLbl = text(layer, { x: 640, y: 322, str: "WRITE SIDE", size: 24, weight: 750, fill: "#8b5cf6", anchor: "middle", spacing: "2px", opacity: 0 });
      rLbl = text(layer, { x: 1280, y: 322, str: "READ SIDE", size: 24, weight: 750, fill: "#22d3ee", anchor: "middle", spacing: "2px", opacity: 0 });

      left = [
        node(layer, { x: 460, y: 356, w: 360, h: 78, title: "Command DTO", accent: "#8b5cf6", titleSize: 23 }),
        node(layer, { x: 460, y: 470, w: 360, h: 78, title: "Command Handler", accent: "#8b5cf6", titleSize: 23 }),
        node(layer, { x: 460, y: 584, w: 360, h: 78, title: "Promotion Aggregate", accent: "#8b5cf6", titleSize: 23 }),
      ];
      right = [
        node(layer, { x: 1100, y: 356, w: 360, h: 78, title: "Query Handler", accent: "#22d3ee", titleSize: 23 }),
        node(layer, { x: 1100, y: 470, w: 360, h: 78, title: "Read Repository", accent: "#22d3ee", titleSize: 23 }),
        node(layer, { x: 1100, y: 584, w: 360, h: 78, title: "Read-model DTO", accent: "#22d3ee", titleSize: 23 }),
      ];

      vEdges = [];
      for (let i = 0; i < 2; i++) {
        vEdges.push(edge(edgeLayer, { from: left[i].bottom, to: left[i + 1].top, color: "#8b5cf6", width: 2.5, bias: 1, arrow: true }));
        vEdges.push(edge(edgeLayer, { from: right[i].bottom, to: right[i + 1].top, color: "#22d3ee", width: 2.5, bias: 1, arrow: true }));
      }

      db = cylinder(layer, 960, 810, 220, 96, "#22d3ee");
      dbEdges = [
        edge(edgeLayer, { from: left[2].bottom, to: { x: 900, y: 770 }, color: "#8b5cf6", width: 2.5, bias: 0.6, arrow: true }),
        edge(edgeLayer, { from: right[2].bottom, to: { x: 1020, y: 770 }, color: "#22d3ee", width: 2.5, bias: 0.6, arrow: true }),
      ];
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      const split = easeOutCubic(clamp((lt - 0.7) / 0.8));
      set(divider, { opacity: split });
      set(wLbl, { opacity: split, transform: `translate(${(1 - split) * 300},0)` });
      set(rLbl, { opacity: split, transform: `translate(${(1 - split) * -300},0)` });

      left.forEach((n, i) => n.setPop(clamp((lt - 1.3 - i * 0.35) / 0.6)));
      right.forEach((n, i) => n.setPop(clamp((lt - 1.5 - i * 0.35) / 0.6)));
      vEdges.forEach((e, i) => drawOn(e, (lt - 2.2 - i * 0.2) / 0.5));
      set(db, { opacity: clamp((lt - 4.0) / 0.7) });
      dbEdges.forEach((e, i) => drawOn(e, (lt - 4.4 - i * 0.2) / 0.5));
    },
  };
}
