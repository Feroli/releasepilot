import { svg, set } from "../dom.js";
import { panel, edge, drawOn, text, codeBlock, pill } from "../kit.js";
import { heading } from "./common.js";
import { clamp, easeOutBack, easeOutCubic } from "../ease.js";

function tableCard(layer, { x, y, w, title, cols, accent, emphasize }) {
  const rowH = 30;
  const h = 48 + cols.length * rowH + 14;
  const g = svg("g", { opacity: 0 });
  panel(g, { x, y, w, h, rx: 14, fill: "rgba(18,14,44,0.9)", stroke: accent, strokeW: emphasize ? 2.4 : 1.4, glow: emphasize ? "glow-soft" : undefined });
  g.appendChild(svg("rect", { x, y, width: w, height: 40, rx: 14, fill: accent, "fill-opacity": 0.22 }));
  text(g, { x: x + 16, y: y + 27, str: title, size: 20, weight: 750, fill: accent, family: "var(--mono)" });
  cols.forEach((c, i) => {
    const isKey = c.includes("PK") || c.includes("FK");
    text(g, { x: x + 16, y: y + 48 + 22 + i * rowH, str: c, size: 17, weight: 500, fill: isKey ? "#cdd3f0" : "#9aa3c7", family: "var(--mono)" });
  });
  layer.appendChild(g);
  const cx = x + w / 2;
  const cy = y + h / 2;
  return { g, x, y, w, h, cx, cy, top: { x: cx, y }, bottom: { x: cx, y: y + h }, left: { x, y: cy }, right: { x: x + w, y: cy }, pop(p) { set(g, { opacity: clamp(p * 1.3), transform: `translate(0,${(1 - easeOutBack(clamp(p))) * 20})` }); } };
}

export function persistence() {
  let head, apps, vers, proms, events, outbox, edges, sqlCard, code, note, lockPill;
  return {
    id: "persistence",
    dur: 11,
    build(layer) {
      head = heading(layer, { kicker: "Persistence", title: "PostgreSQL write model" });
      const el = svg("g");
      layer.appendChild(el);

      apps = tableCard(layer, { x: 140, y: 330, w: 250, title: "applications", cols: ["id PK", "name"], accent: "#22d3ee" });
      vers = tableCard(layer, { x: 140, y: 520, w: 250, title: "application_versions", cols: ["id PK", "application_id FK", "version"], accent: "#22d3ee" });
      proms = tableCard(layer, { x: 470, y: 360, w: 320, title: "promotions", cols: ["id PK", "application_id FK", "version_id FK", "status", "requested_by", "approved_by", "aggregate_version"], accent: "#8b5cf6", emphasize: true });
      events = tableCard(layer, { x: 860, y: 330, w: 290, title: "promotion_events", cols: ["id PK", "promotion_id FK", "event_type", "payload jsonb"], accent: "#a3e635" });
      outbox = tableCard(layer, { x: 860, y: 560, w: 290, title: "outbox_events", cols: ["id PK", "event_type", "payload jsonb", "published_at"], accent: "#fbbf24" });

      edges = [
        edge(el, { from: apps.bottom, to: vers.top, color: "#22d3ee", width: 2, bias: 1, arrow: true }),
        edge(el, { from: vers.right, to: proms.left, color: "#8b5cf6", width: 2, bias: 0.2, arrow: true }),
        edge(el, { from: proms.right, to: events.left, color: "#a3e635", width: 2, bias: 0.2, arrow: true }),
        edge(el, { from: proms.right, to: outbox.left, color: "#fbbf24", width: 2, bias: 0.3, arrow: true }),
      ];

      sqlCard = panel(layer, { x: 1210, y: 348, w: 620, h: 404, rx: 18, fill: "rgba(10,8,30,0.92)", stroke: "rgba(163,230,53,0.5)", strokeW: 1.8, glow: "glow-soft" });
      set(sqlCard, { opacity: 0 });
      text(layer, { x: 1234, y: 388, str: "the concurrency guard", size: 18, weight: 700, fill: "#a3e635", family: "var(--mono)", id: "sql-kicker", opacity: 0 });
      code = codeBlock(layer, {
        x: 1234,
        y: 428,
        size: 21,
        gap: 31,
        lines: [
          "CREATE UNIQUE INDEX",
          "  ux_active_promotion_target",
          "ON promotions(application_id,",
          "              target_environment)",
          "WHERE status IN ('REQUESTED',",
          "      'APPROVED','DEPLOYING');",
        ],
        colorFn: (ln) => (ln.includes("WHERE") || ln.includes("REQUESTED") ? "#fbbf24" : "#cdd3f0"),
      });
      note = text(layer, { x: 1234, y: 640, str: "→ one active promotion per app + target", size: 19, weight: 600, fill: "#a3e635", opacity: 0 });
      lockPill = pill(layer, { x: 1234, y: 690, w: 400, h: 44, str: "optimistic lock · aggregate_version", color: "#8b5cf6", size: 18 });
    },
    render(lt) {
      head.reveal((lt - 0.1) / 0.9);
      [apps, vers, proms, events, outbox].forEach((c, i) => c.pop(clamp((lt - 0.8 - i * 0.35) / 0.6)));
      edges.forEach((e, i) => drawOn(e, (lt - 2.4 - i * 0.25) / 0.5));

      const sp = easeOutCubic(clamp((lt - 4.4) / 0.7));
      set(sqlCard, { opacity: sp, transform: `translate(${(1 - sp) * 60},0)` });
      const k = document.getElementById("sql-kicker");
      if (k) {
        set(k, { opacity: sp });
      }
      code.setReveal((lt - 5.0) * 42);
      set(note, { opacity: clamp((lt - 8.0) / 0.6) });
      set(lockPill.g, { opacity: clamp((lt - 8.6) / 0.6) });
    },
  };
}
