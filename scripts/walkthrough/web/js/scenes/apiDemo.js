import { svg, set } from "../dom.js";
import { panel, text, codeBlock } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutCubic, easeOutBack } from "../ease.js";

const HISTORY = [
  { at: 3.4, type: "PromotionRequested", user: "fernando", color: "#22d3ee" },
  { at: 5.2, type: "PromotionApproved", user: "release-manager", color: "#8b5cf6" },
  { at: 7.2, type: "DeploymentStarted", user: "release-manager", color: "#fbbf24" },
  { at: 9.2, type: "PromotionCompleted", user: "release-manager", color: "#a3e635" },
];
const STATES = [
  { at: 3.0, label: "REQUESTED", color: "#22d3ee" },
  { at: 5.0, label: "APPROVED", color: "#8b5cf6" },
  { at: 7.0, label: "DEPLOYING", color: "#fbbf24" },
  { at: 9.0, label: "COMPLETED", color: "#a3e635" },
];
const CMDS = [
  { at: 4.6, str: "POST /promotions/{id}/approve" },
  { at: 6.6, str: "POST /promotions/{id}/deployments" },
  { at: 8.6, str: "POST /promotions/{id}/complete" },
];

export function apiDemo() {
  let head, leftCard, code, rightCard, statusRect, statusText, histEls, cmdEl;
  return {
    id: "apiDemo",
    dur: 14,
    build(layer) {
      head = heading(layer, { kicker: "In practice", title: "Request, drive, then query history" });

      leftCard = svg("g", { opacity: 0 });
      panel(leftCard, { x: 110, y: 300, w: 830, h: 470, rx: 18, fill: "rgba(8,7,26,0.94)", stroke: "rgba(34,211,238,0.5)", glow: "glow-soft" });
      panel(leftCard, { x: 110, y: 300, w: 830, h: 44, rx: 18, fill: "rgba(34,211,238,0.16)" });
      text(leftCard, { x: 138, y: 330, str: "POST /promotions", size: 22, weight: 700, fill: "#22d3ee", family: "var(--mono)" });
      layer.appendChild(leftCard);
      code = codeBlock(layer, {
        x: 146,
        y: 402,
        size: 25,
        gap: 44,
        lines: [
          "{",
          '  "applicationId": "payments-api",',
          '  "version": "1.4.0",',
          '  "sourceEnvironment": "DEV",',
          '  "targetEnvironment": "STAGING",',
          '  "requestedBy": "fernando"',
          "}",
        ],
        colorFn: (ln) => (ln.includes(":") ? "#cdd3f0" : "#8b5cf6"),
      });

      rightCard = svg("g", { opacity: 0 });
      panel(rightCard, { x: 980, y: 300, w: 830, h: 470, rx: 18, fill: "rgba(8,7,26,0.94)", stroke: "rgba(163,230,53,0.45)", glow: "glow-soft" });
      panel(rightCard, { x: 980, y: 300, w: 830, h: 44, rx: 18, fill: "rgba(163,230,53,0.14)" });
      text(rightCard, { x: 1008, y: 330, str: "GET /promotions/{id}", size: 22, weight: 700, fill: "#a3e635", family: "var(--mono)" });
      cmdEl = text(rightCard, { x: 1790, y: 330, str: "", size: 17, weight: 600, fill: "#fbbf24", family: "var(--mono)", anchor: "end" });
      statusRect = svg("rect", { x: 1008, y: 372, width: 300, height: 58, rx: 29, fill: "#22d3ee", "fill-opacity": 0.16, stroke: "#22d3ee", "stroke-width": 2 });
      rightCard.appendChild(statusRect);
      statusText = text(rightCard, { x: 1158, y: 411, str: "status: —", size: 26, weight: 750, fill: "#22d3ee", anchor: "middle" });
      text(rightCard, { x: 1008, y: 486, str: "history", size: 18, weight: 700, fill: "#5b6088", family: "var(--mono)" });
      layer.appendChild(rightCard);

      histEls = HISTORY.map((h, i) => {
        const y = 516 + i * 62;
        const g = svg("g", { opacity: 0 });
        g.appendChild(svg("circle", { cx: 1024, cy: y + 8, r: 7, fill: h.color, filter: "url(#glow-soft)" }));
        text(g, { x: 1048, y: y + 15, str: h.type, size: 23, weight: 600, fill: "#f5f7ff", family: "var(--mono)" });
        text(g, { x: 1790, y: y + 15, str: h.user, size: 20, weight: 500, fill: "#9aa3c7", anchor: "end", family: "var(--mono)" });
        rightCard.appendChild(g);
        return g;
      });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      set(leftCard, { opacity: easeOutCubic(clamp((lt - 0.3) / 0.5)) });
      code.setReveal((lt - 0.7) * 42);
      set(rightCard, { opacity: easeOutCubic(clamp((lt - 2.3) / 0.5)) });

      // status steps through states
      let cur = STATES[0];
      for (const s of STATES) {
        if (lt >= s.at) {
          cur = s;
        }
      }
      if (lt >= STATES[0].at) {
        statusText.textContent = `status: ${cur.label}`;
        set(statusText, { fill: cur.color });
        set(statusRect, { fill: cur.color, stroke: cur.color });
        // pop pulse right after a transition
        const since = lt - cur.at;
        const pulse = since < 0.4 ? easeOutBack(clamp(since / 0.4)) : 1;
        set(statusRect, { transform: `translate(1158,401) scale(${0.9 + 0.1 * pulse}) translate(-1158,-401)` });
      }

      // command ticker flashes near each transition
      let cmd = null;
      for (const c of CMDS) {
        if (lt >= c.at && lt < c.at + 1.4) {
          cmd = c.str;
        }
      }
      cmdEl.textContent = cmd || "";

      // history entries append
      HISTORY.forEach((h, i) => {
        const p = clamp((lt - h.at) / 0.5);
        set(histEls[i], { opacity: clamp(p * 1.3), transform: `translate(${(1 - easeOutBack(p)) * -24},0)` });
      });
    },
  };
}
