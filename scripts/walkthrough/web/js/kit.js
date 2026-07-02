// Reusable, deterministic drawing primitives. Builders create structure once;
// scenes animate by mutating handles each frame via the returned setter helpers.

import { svg, set } from "./dom.js";
import { clamp, lerp, easeOutBack } from "./ease.js";

// ---- text ----------------------------------------------------------------

export function text(parent, { x, y, str, size = 30, weight = 500, fill = "#f5f7ff", anchor = "start", family, spacing, opacity = 1, id }) {
  const t = svg("text", {
    x,
    y,
    "font-size": size,
    "font-weight": weight,
    fill,
    "text-anchor": anchor,
    "font-family": family || "var(--sans)",
    "letter-spacing": spacing,
    opacity,
    id,
    style: "dominant-baseline:alphabetic",
  });
  t.textContent = str;
  parent.appendChild(t);
  return t;
}

export function title(parent, { x, y, str, size = 66, fill = "#f5f7ff", anchor = "start", weight = 750 }) {
  return text(parent, { x, y, str, size, weight, fill, anchor, spacing: "-0.5px" });
}

export function multiline(parent, { x, y, lines, size = 28, gap = 42, fill = "#9aa3c7", weight = 500, anchor = "start" }) {
  const nodes = [];
  lines.forEach((ln, i) => {
    nodes.push(text(parent, { x, y: y + i * gap, str: ln, size, weight, fill, anchor }));
  });
  return nodes;
}

// ---- panels & nodes ------------------------------------------------------

export function panel(parent, { x, y, w, h, rx = 22, fill = "url(#grad-panel)", stroke = "rgba(139,129,216,0.30)", strokeW = 1.5, glow, opacity = 1 }) {
  const r = svg("rect", {
    x,
    y,
    width: w,
    height: h,
    rx,
    ry: rx,
    fill,
    stroke,
    "stroke-width": strokeW,
    opacity,
    filter: glow ? `url(#${glow})` : undefined,
  });
  parent.appendChild(r);
  return r;
}

// A labelled node box that can "pop" in. Internal shapes live at local (0..w,0..h);
// the group is translated to (x,y). setPop(p, dy) animates scale-around-center + opacity.
export function node(parent, { x, y, w, h, title: ttl, sub, accent = "#8b5cf6", rx = 18, titleSize = 27, subSize = 16, fill = "url(#grad-panel)", glow = "glow-soft" }) {
  const g = svg("g", { transform: `translate(${x},${y})`, opacity: 0 });
  const rect = svg("rect", {
    x: 0,
    y: 0,
    width: w,
    height: h,
    rx,
    ry: rx,
    fill,
    stroke: accent,
    "stroke-width": 2,
    filter: `url(#${glow})`,
  });
  g.appendChild(rect);
  // accent top bar
  g.appendChild(svg("rect", { x: 0, y: 0, width: w, height: 5, rx: 3, ry: 3, fill: accent, opacity: 0.9 }));

  const tEl = text(g, {
    x: w / 2,
    y: sub ? h / 2 - 4 : h / 2 + 9,
    str: ttl,
    size: titleSize,
    weight: 700,
    anchor: "middle",
  });
  let sEl = null;
  if (sub) {
    sEl = text(g, { x: w / 2, y: h / 2 + 24, str: sub, size: subSize, weight: 500, fill: "#9aa3c7", anchor: "middle" });
  }
  parent.appendChild(g);

  const cx = x + w / 2;
  const cy = y + h / 2;
  const handle = {
    g,
    rect,
    titleEl: tEl,
    subEl: sEl,
    x,
    y,
    w,
    h,
    cx,
    cy,
    // anchor points on the box edges, in stage coordinates
    top: { x: cx, y },
    bottom: { x: cx, y: y + h },
    left: { x, y: cy },
    right: { x: x + w, y: cy },
    setPop(p, dy = 0) {
      const s = lerp(0.82, 1, easeOutBack(clamp(p)));
      set(g, {
        opacity: clamp(p * 1.3),
        transform: `translate(${x},${y + (1 - clamp(p)) * (18 + dy)}) translate(${w / 2},${h / 2}) scale(${s}) translate(${-w / 2},${-h / 2})`,
      });
    },
    highlight(on, color) {
      set(rect, { "stroke-width": on ? 4 : 2, stroke: color || accent });
    },
  };
  return handle;
}

// ---- edges & travelling packets -----------------------------------------

// Smooth cubic connector between two stage points. `bias` biases the control
// handles (0 = mostly horizontal S-curve, 1 = mostly vertical).
export function edge(parent, { from, to, color = "rgba(139,129,216,0.55)", width = 2.5, dash, bias = 0.5, arrow = false, glow }) {
  const dx = to.x - from.x;
  const dy = to.y - from.y;
  const hx = lerp(dx, 0, bias) * 0.5;
  const hy = lerp(0, dy, bias) * 0.5;
  const c1 = { x: from.x + hx, y: from.y + hy };
  const c2 = { x: to.x - hx, y: to.y - hy };
  const d = `M ${from.x} ${from.y} C ${c1.x} ${c1.y} ${c2.x} ${c2.y} ${to.x} ${to.y}`;
  const path = svg("path", {
    d,
    fill: "none",
    stroke: color,
    "stroke-width": width,
    "stroke-linecap": "round",
    "stroke-dasharray": dash,
    filter: glow ? `url(#${glow})` : undefined,
  });
  parent.appendChild(path);
  if (arrow) {
    // small triangle at the end, oriented along the final segment
    const ang = Math.atan2(to.y - c2.y, to.x - c2.x);
    const a = 10;
    const p1 = `${to.x},${to.y}`;
    const p2 = `${to.x - a * Math.cos(ang - 0.4)},${to.y - a * Math.sin(ang - 0.4)}`;
    const p3 = `${to.x - a * Math.cos(ang + 0.4)},${to.y - a * Math.sin(ang + 0.4)}`;
    parent.appendChild(svg("polygon", { points: `${p1} ${p2} ${p3}`, fill: color }));
  }
  return path;
}

// Reveal a path progressively (draw-on) using dashoffset. p: 0..1
export function drawOn(path, p) {
  const len = path.getTotalLength();
  set(path, { "stroke-dasharray": len, "stroke-dashoffset": len * (1 - clamp(p)) });
}

export function packet(parent, { r = 8, color = "#22d3ee", glow = "glow-strong" }) {
  const c = svg("circle", { r, fill: color, filter: `url(#${glow})`, opacity: 0 });
  parent.appendChild(c);
  return c;
}

// Place a node on a path at progress p (0..1). Returns the point.
export function placeOnPath(node, path, p) {
  const len = path.getTotalLength();
  const pt = path.getPointAtLength(len * clamp(p));
  set(node, { cx: pt.x, cy: pt.y });
  return pt;
}

// ---- small components -----------------------------------------------------

export function pill(parent, { x, y, w, h = 44, str, color = "#22d3ee", fillOpacity = 0.14, size = 20 }) {
  const g = svg("g", { opacity: 0 });
  g.appendChild(
    svg("rect", { x, y, width: w, height: h, rx: h / 2, ry: h / 2, fill: color, "fill-opacity": fillOpacity, stroke: color, "stroke-width": 1.4 })
  );
  const t = text(g, { x: x + w / 2, y: y + h / 2 + size * 0.34, str, size, weight: 600, fill: color, anchor: "middle" });
  parent.appendChild(g);
  return { g, textEl: t };
}

export function chip(parent, { x, y, str, color = "#9aa3c7", size = 19 }) {
  const w = str.length * size * 0.62 + 34;
  return pill(parent, { x, y, w, h: 40, str, color, size });
}

// A left-aligned code/JSON block drawn as monospace SVG text lines, supporting a
// typewriter reveal via setReveal(charsShown).
export function codeBlock(parent, { x, y, lines, size = 23, gap = 34, colorFn }) {
  const els = [];
  const flat = [];
  lines.forEach((ln, i) => {
    const t = text(parent, {
      x,
      y: y + i * gap,
      str: "",
      size,
      weight: 500,
      family: "var(--mono)",
      fill: colorFn ? colorFn(ln, i) : "#cdd3f0",
      anchor: "start",
    });
    els.push(t);
    flat.push(ln);
  });
  const total = flat.reduce((a, l) => a + l.length + 1, 0);
  return {
    els,
    total,
    setReveal(chars) {
      let remaining = Math.max(0, Math.floor(chars));
      for (let i = 0; i < flat.length; i++) {
        const take = Math.min(flat[i].length, remaining);
        els[i].textContent = flat[i].slice(0, take);
        remaining -= flat[i].length + 1;
        if (remaining < 0) {
          remaining = 0;
        }
      }
    },
    setAll() {
      for (let i = 0; i < flat.length; i++) {
        els[i].textContent = flat[i];
      }
    },
  };
}
