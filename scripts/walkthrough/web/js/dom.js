// Thin SVG/DOM construction helpers used by the kit and scenes.

const SVGNS = "http://www.w3.org/2000/svg";

export function svg(tag, attrs = {}, children = []) {
  const node = document.createElementNS(SVGNS, tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (v === undefined || v === null) {
      continue;
    }
    node.setAttribute(k, String(v));
  }
  for (const c of children) {
    if (c) {
      node.appendChild(c);
    }
  }
  return node;
}

export function set(node, attrs) {
  for (const [k, v] of Object.entries(attrs)) {
    if (v === undefined || v === null) {
      continue;
    }
    node.setAttribute(k, String(v));
  }
  return node;
}

// Install shared <defs>: gradients + glow filters reused across every scene.
export function installDefs(root) {
  const defs = svg("defs");

  defs.appendChild(
    linearGradient("grad-beam", [
      [0, "#22d3ee"],
      [0.5, "#8b5cf6"],
      [1, "#ec4899"],
    ])
  );
  defs.appendChild(
    linearGradient("grad-cool", [
      [0, "#22d3ee"],
      [1, "#3b82f6"],
    ])
  );
  defs.appendChild(
    linearGradient("grad-success", [
      [0, "#a3e635"],
      [1, "#22d3ee"],
    ])
  );
  defs.appendChild(
    linearGradient("grad-warn", [
      [0, "#fbbf24"],
      [1, "#ec4899"],
    ])
  );
  defs.appendChild(
    linearGradient("grad-panel", [
      [0, "rgba(46,38,96,0.9)"],
      [1, "rgba(22,16,58,0.9)"],
    ])
  );

  defs.appendChild(glow("glow-soft", 3.2));
  defs.appendChild(glow("glow-strong", 6));
  defs.appendChild(glow("glow-huge", 12));

  root.appendChild(defs);
}

function linearGradient(id, stops, x1 = 0, y1 = 0, x2 = 1, y2 = 1) {
  const g = svg("linearGradient", { id, x1, y1, x2, y2 });
  for (const [offset, color] of stops) {
    g.appendChild(svg("stop", { offset, "stop-color": color }));
  }
  return g;
}

function glow(id, deviation) {
  const f = svg("filter", {
    id,
    x: "-60%",
    y: "-60%",
    width: "220%",
    height: "220%",
  });
  f.appendChild(svg("feGaussianBlur", { in: "SourceGraphic", stdDeviation: deviation, result: "b" }));
  const merge = svg("feMerge");
  merge.appendChild(svg("feMergeNode", { in: "b" }));
  merge.appendChild(svg("feMergeNode", { in: "SourceGraphic" }));
  f.appendChild(merge);
  return f;
}

// Convenience group with a display toggle used by the timeline.
export function group(id) {
  return svg("g", { id, style: "display:none" });
}
