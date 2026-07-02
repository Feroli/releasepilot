// Bootstrap: build every scene once (hidden), then expose a deterministic
// window.seek(t) that the frame renderer drives. No wall-clock animation.

import { installDefs } from "./dom.js";
import { buildBackground, updateBackground, applyCamera } from "./camera.js";
import { updateCaptions } from "./captions.js";
import { SCENES, TOTAL } from "./timeline.js";
import { clamp, smoothstep } from "./ease.js";

const SVGNS = "http://www.w3.org/2000/svg";
const TRANS = 0.42; // scene-entry light-sweep window

const scene = document.getElementById("scene");
installDefs(scene);

let particles;
let camera;

function build() {
  particles = buildBackground(scene);

  camera = document.createElementNS(SVGNS, "g");
  camera.setAttribute("id", "camera");
  scene.appendChild(camera);

  const scenes = document.createElementNS(SVGNS, "g");
  scenes.setAttribute("id", "scenes");
  camera.appendChild(scenes);

  for (const s of SCENES) {
    const g = document.createElementNS(SVGNS, "g");
    g.setAttribute("class", "scene");
    g.style.display = "none";
    s.layer = g;
    s.build(g);
    scenes.appendChild(g);
  }

  document.getElementById("scene-total").textContent = String(SCENES.length).padStart(2, "0");
}

let activeIdx = -1;

function seek(t) {
  t = clamp(t, 0, TOTAL - 0.0001);

  updateBackground(particles, t);
  applyCamera(camera, t);

  let idx = 0;
  for (let i = 0; i < SCENES.length; i++) {
    if (t >= SCENES[i].start) {
      idx = i;
    }
  }
  const s = SCENES[idx];
  const lt = t - s.start;

  if (idx !== activeIdx) {
    for (let i = 0; i < SCENES.length; i++) {
      SCENES[i].layer.style.display = i === idx ? "block" : "none";
    }
    activeIdx = idx;
    document.getElementById("scene-index").textContent = String(idx + 1).padStart(2, "0");
  }

  s.render(lt, s.dur);
  updateCaptions(t);

  document.getElementById("progress-fill").style.width = `${(t / TOTAL) * 100}%`;

  // entry light-sweep transition (skip the very first scene)
  const flash = document.getElementById("flash");
  if (idx > 0 && lt < TRANS) {
    const p = lt / TRANS;
    flash.style.opacity = String(smoothstep(0, 0.3, p) * (1 - smoothstep(0.6, 1, p)));
    flash.style.transform = `translateX(${(p * 140 - 70).toFixed(2)}%)`;
  } else {
    flash.style.opacity = "0";
  }
}

build();
window.TOTAL_DURATION = TOTAL;
window.seek = seek;
seek(0);
window.__ready = true;
