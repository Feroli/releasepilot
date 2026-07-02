// Smoke test: load the app, trap JS errors, and capture one frame per scene
// midpoint (and a couple of transition moments) into <outDir> for visual review.
import http from "node:http";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { chromium } from "playwright";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const WEB_DIR = path.join(__dirname, "web");
const outDir = process.argv[2] || "/tmp/rp-smoke";
fs.mkdirSync(outDir, { recursive: true });

const MIME = { ".html": "text/html", ".css": "text/css", ".js": "text/javascript", ".json": "application/json", ".svg": "image/svg+xml" };

const server = http.createServer((req, res) => {
  const p = decodeURIComponent(req.url.split("?")[0]);
  const fp = path.join(WEB_DIR, p === "/" ? "index.html" : p);
  fs.readFile(fp, (err, data) => {
    if (err) { res.writeHead(404); res.end(); return; }
    res.writeHead(200, { "content-type": MIME[path.extname(fp)] || "application/octet-stream" });
    res.end(data);
  });
});
await new Promise((r) => server.listen(0, "127.0.0.1", r));
const { port } = server.address();

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage({ viewport: { width: 1920, height: 1080 } });

const errors = [];
page.on("pageerror", (e) => errors.push("PAGEERROR: " + e.message));
page.on("console", (m) => { if (m.type() === "error") errors.push("CONSOLE: " + m.text()); });

await page.goto(`http://127.0.0.1:${port}/index.html`, { waitUntil: "load" });
await page.waitForFunction("window.__ready === true", null, { timeout: 30000 });

const scenes = await page.evaluate(() => {
  const t = window.__timeline || null;
  return null;
});

// pull scene starts/durs from the page
const meta = await page.evaluate(async () => {
  const mod = await import("./js/timeline.js");
  return { total: mod.TOTAL, scenes: mod.SCENES.map((s) => ({ id: s.id, start: s.start, dur: s.dur })) };
});

console.log("TOTAL:", meta.total, "s");
for (let i = 0; i < meta.scenes.length; i++) {
  const s = meta.scenes[i];
  const mid = s.start + s.dur * 0.62;
  await page.evaluate((t) => window.seek(t), mid);
  const name = `${String(i + 1).padStart(2, "0")}-${s.id}.png`;
  await page.screenshot({ path: path.join(outDir, name), clip: { x: 0, y: 0, width: 1920, height: 1080 } });
}
// a couple of transition moments
for (const t of [9.15, 45.15, 139.15]) {
  await page.evaluate((tt) => window.seek(tt), t);
  await page.screenshot({ path: path.join(outDir, `trans-${t}.png`), clip: { x: 0, y: 0, width: 1920, height: 1080 } });
}

await browser.close();
server.close();

if (errors.length) {
  console.log("\n=== ERRORS (" + errors.length + ") ===");
  console.log([...new Set(errors)].slice(0, 40).join("\n"));
  process.exit(2);
}
console.log("\nno JS errors. frames in " + outDir);
