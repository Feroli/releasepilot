import { svg, set } from "../dom.js";
import { node, edge, drawOn, packet, placeOnPath, text } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutCubic, easeOutBack, easeInOutCubic } from "../ease.js";

const CX = 980;
const CY = 560;

function hexPoints(cx, cy, r) {
  const pts = [];
  for (let i = 0; i < 6; i++) {
    const a = (Math.PI / 180) * (60 * i);
    pts.push([cx + r * Math.cos(a), cy + r * Math.sin(a)]);
  }
  return pts.map((p) => p.join(",")).join(" ");
}

export function hexagon() {
  let head, outer, inner, domainT, domainSub, appLbl, inbound, infra, inEdges, outEdges, pkin, pkout;
  return {
    id: "hexagon",
    dur: 13,
    build(layer) {
      head = heading(layer, { kicker: "Structure", title: "Hexagonal — a pure core" });

      const edgeLayer = svg("g");
      layer.appendChild(edgeLayer);

      outer = svg("polygon", { points: hexPoints(CX, CY, 300), fill: "rgba(139,92,246,0.06)", stroke: "url(#grad-beam)", "stroke-width": 3, filter: "url(#glow-soft)", opacity: 0 });
      layer.appendChild(outer);
      inner = svg("polygon", { points: hexPoints(CX, CY, 168), fill: "rgba(34,211,238,0.10)", stroke: "#22d3ee", "stroke-width": 2.5, filter: "url(#glow-soft)", opacity: 0 });
      layer.appendChild(inner);

      domainT = text(layer, { x: CX, y: CY - 6, str: "DOMAIN", size: 34, weight: 750, fill: "#22d3ee", anchor: "middle", opacity: 0 });
      domainSub = text(layer, { x: CX, y: CY + 32, str: "Promotion · Pipeline · Events", size: 19, weight: 500, fill: "#9aa3c7", anchor: "middle", opacity: 0 });
      appLbl = text(layer, { x: CX, y: CY - 232, str: "APPLICATION · handlers + ports", size: 20, weight: 650, fill: "#8b5cf6", anchor: "middle", opacity: 0 });

      inbound = [
        node(layer, { x: 120, y: 400, w: 300, h: 104, title: "REST Controllers", accent: "#22d3ee", titleSize: 24 }),
        node(layer, { x: 120, y: 620, w: 300, h: 104, title: "Message Consumers", accent: "#22d3ee", titleSize: 24 }),
      ];
      infra = [
        node(layer, { x: 1500, y: 360, w: 320, h: 96, title: "JPA · PostgreSQL", accent: "#a3e635", titleSize: 23 }),
        node(layer, { x: 1500, y: 512, w: 320, h: 96, title: "RabbitMQ Publisher", accent: "#8b5cf6", titleSize: 23 }),
        node(layer, { x: 1500, y: 664, w: 320, h: 96, title: "Deployment / Issue / Notify Stubs", accent: "#ec4899", titleSize: 19 }),
      ];

      inEdges = inbound.map((b) => edge(edgeLayer, { from: { x: b.x + b.w, y: b.cy }, to: { x: CX - 200, y: b.cy }, color: "#22d3ee", width: 2.5, bias: 0.15, arrow: true }));
      outEdges = infra.map((b) => edge(edgeLayer, { from: { x: CX + 200, y: b.cy }, to: { x: b.x, y: b.cy }, color: b === infra[2] ? "#ec4899" : "#8b5cf6", width: 2.5, bias: 0.15, arrow: true }));

      pkin = packet(layer, { r: 8, color: "#22d3ee" });
      pkout = packet(layer, { r: 8, color: "#a3e635" });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      const op = easeOutCubic(clamp((lt - 0.7) / 0.9));
      set(outer, { opacity: op, transform: `translate(${CX},${CY}) scale(${0.85 + 0.15 * op}) translate(${-CX},${-CY})` });
      set(inner, { opacity: easeOutCubic(clamp((lt - 1.3) / 0.9)) });
      set(domainT, { opacity: clamp((lt - 1.8) / 0.6) });
      set(domainSub, { opacity: clamp((lt - 2.1) / 0.6) });
      set(appLbl, { opacity: clamp((lt - 2.4) / 0.6) });

      inbound.forEach((b, i) => {
        const p = clamp((lt - 3.0 - i * 0.35) / 0.6);
        set(b.g, { opacity: clamp(p * 1.3), transform: `translate(${b.x + (1 - easeOutBack(p)) * -60},${b.y})` });
        drawOn(inEdges[i], (lt - 3.4 - i * 0.35) / 0.6);
      });
      infra.forEach((b, i) => {
        const p = clamp((lt - 4.4 - i * 0.3) / 0.6);
        set(b.g, { opacity: clamp(p * 1.3), transform: `translate(${b.x + (1 - easeOutBack(p)) * 60},${b.y})` });
        drawOn(outEdges[i], (lt - 4.8 - i * 0.3) / 0.6);
      });

      // packet flows inbound then outbound
      const inP = clamp((lt - 6.4) / 1.6);
      set(pkin, { opacity: inP > 0 && inP < 1 ? 1 : 0 });
      if (inP > 0 && inP < 1) {
        placeOnPath(pkin, inEdges[0], easeInOutCubic(inP));
      }
      const outP = clamp((lt - 8.4) / 1.6);
      set(pkout, { opacity: outP > 0 && outP < 1 ? 1 : 0 });
      if (outP > 0 && outP < 1) {
        placeOnPath(pkout, outEdges[0], easeInOutCubic(outP));
      }
    },
  };
}
