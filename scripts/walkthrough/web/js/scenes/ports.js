import { svg, set } from "../dom.js";
import { node, edge, drawOn, text } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutBack } from "../ease.js";

const PAIRS = [
  { port: "DeploymentPort", stub: "StubDeploymentAdapter", color: "#22d3ee" },
  { port: "IssueTrackerPort", stub: "StubIssueTrackerAdapter", color: "#8b5cf6" },
  { port: "NotificationPort", stub: "LoggingNotificationAdapter", color: "#a3e635" },
  { port: "ApproverDirectory", stub: "InMemoryApproverDirectory", color: "#ec4899" },
];

export function ports() {
  let head, core, portNodes, stubNodes, coreEdges, implEdges, tags, note;
  return {
    id: "ports",
    dur: 11,
    build(layer) {
      head = heading(layer, { kicker: "Boundaries", title: "Ports & replaceable adapters" });
      const el = svg("g");
      layer.appendChild(el);

      core = node(layer, { x: 110, y: 400, w: 290, h: 300, title: "Application", sub: "depends only on ports", accent: "#8b5cf6", titleSize: 30, subSize: 18, glow: "glow-strong" });

      portNodes = [];
      stubNodes = [];
      coreEdges = [];
      implEdges = [];
      tags = [];
      PAIRS.forEach((pr, i) => {
        const y = 336 + i * 118;
        const pn = node(layer, { x: 500, y, w: 360, h: 96, title: pr.port, accent: pr.color, titleSize: 24 });
        const sn = node(layer, { x: 1170, y, w: 400, h: 96, title: pr.stub, accent: pr.color, titleSize: 21, fill: "rgba(20,18,48,0.5)" });
        portNodes.push(pn);
        stubNodes.push(sn);
        tags.push(text(layer, { x: 520, y: y + 22, str: "«interface»", size: 15, weight: 600, fill: "#5b6088", family: "var(--mono)", opacity: 0 }));
        coreEdges.push(edge(el, { from: core.right, to: pn.left, color: pr.color, width: 2.2, bias: 0.5 }));
        implEdges.push(edge(el, { from: pn.right, to: sn.left, color: pr.color, width: 2.2, bias: 0, arrow: true, dash: "8 7" }));
      });

      note = text(layer, { x: 960, y: 858, str: "swap a stub for a real client — the core never changes", size: 22, weight: 600, fill: "#9aa3c7", anchor: "middle", opacity: 0 });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      core.setPop(clamp((lt - 0.7) / 0.7));
      portNodes.forEach((n, i) => {
        const p = clamp((lt - 1.4 - i * 0.3) / 0.6);
        n.setPop(p);
        set(tags[i], { opacity: clamp((lt - 1.7 - i * 0.3) / 0.5) });
        drawOn(coreEdges[i], (lt - 1.6 - i * 0.3) / 0.5);
      });
      stubNodes.forEach((n, i) => {
        const p = clamp((lt - 3.4 - i * 0.3) / 0.6);
        set(n.g, { opacity: clamp(p * 1.3), transform: `translate(${n.x + (1 - easeOutBack(p)) * 60},${n.y})` });
        drawOn(implEdges[i], (lt - 3.6 - i * 0.3) / 0.5);
      });
      set(note, { opacity: clamp((lt - 6.6) / 0.7) });
    },
  };
}
