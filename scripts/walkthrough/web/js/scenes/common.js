// Shared scene helpers: the standard heading block and reveal utilities.

import { svg, set } from "../dom.js";
import { text } from "../kit.js";
import { clamp, easeOutCubic, easeOutBack } from "../ease.js";

// Standard top-left heading: small kicker + big title + growing accent rule.
export function heading(layer, { kicker, title, x = 120, y = 190, color = "#22d3ee" }) {
  const g = svg("g");
  const kick = text(g, { x, y: y - 52, str: kicker.toUpperCase(), size: 21, weight: 700, fill: color, spacing: "4px" });
  const ttl = text(g, { x, y, str: title, size: 62, weight: 750, fill: "#f5f7ff", spacing: "-0.5px" });
  const rule = svg("rect", { x, y: y + 26, width: 0, height: 5, rx: 3, fill: color, filter: "url(#glow-soft)" });
  g.appendChild(rule);
  layer.appendChild(g);
  return {
    g,
    reveal(p) {
      const e = easeOutCubic(clamp(p));
      const dx = (1 - e) * -34;
      set(kick, { opacity: clamp(p * 2), transform: `translate(${dx},0)` });
      set(ttl, { opacity: clamp(p * 1.4), transform: `translate(${dx * 0.7},0)` });
      set(rule, { width: 132 * e });
    },
  };
}

// Fade + rise-in for a plain element.
export function riseIn(node, p, dist = 22) {
  const e = easeOutBack(clamp(p));
  set(node, { opacity: clamp(p * 1.3), transform: `translate(0,${(1 - clamp(p)) * dist})` });
}

// Opacity-only fade with a curve.
export function fade(node, p) {
  set(node, { opacity: clamp(easeOutCubic(clamp(p)) * 1.05) });
}
