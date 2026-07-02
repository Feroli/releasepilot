// Deterministic frame renderer: serve the web app over localhost, drive
// window.seek(frame/FPS) through headless Chromium, screenshot every frame.
//
// Usage: node render.mjs <framesDir> [fps]

import http from "node:http";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { chromium } from "playwright";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const WEB_DIR = path.join(__dirname, "web");

const framesDir = process.argv[2];
const FPS = Number(process.argv[3] || process.env.FPS || 30);
if (!framesDir) {
  console.error("usage: node render.mjs <framesDir> [fps]");
  process.exit(1);
}
fs.mkdirSync(framesDir, { recursive: true });

const MIME = {
  ".html": "text/html",
  ".css": "text/css",
  ".js": "text/javascript",
  ".mjs": "text/javascript",
  ".json": "application/json",
  ".svg": "image/svg+xml",
};

function startServer() {
  return new Promise((resolve) => {
    const server = http.createServer((req, res) => {
      const urlPath = decodeURIComponent(req.url.split("?")[0]);
      let filePath = path.join(WEB_DIR, urlPath === "/" ? "index.html" : urlPath);
      if (!filePath.startsWith(WEB_DIR)) {
        res.writeHead(403);
        res.end();
        return;
      }
      fs.readFile(filePath, (err, data) => {
        if (err) {
          res.writeHead(404);
          res.end("not found");
          return;
        }
        res.writeHead(200, { "content-type": MIME[path.extname(filePath)] || "application/octet-stream" });
        res.end(data);
      });
    });
    server.listen(0, "127.0.0.1", () => resolve(server));
  });
}

async function launchBrowser() {
  try {
    return await chromium.launch({ headless: true });
  } catch (e) {
    console.warn("bundled chromium unavailable, falling back to system Chrome:", e.message);
    return await chromium.launch({ headless: true, channel: "chrome" });
  }
}

async function main() {
  const server = await startServer();
  const { port } = server.address();
  const browser = await launchBrowser();
  const page = await browser.newPage({ viewport: { width: 1920, height: 1080 }, deviceScaleFactor: 1 });
  await page.goto(`http://127.0.0.1:${port}/index.html`, { waitUntil: "load" });
  await page.waitForFunction("window.__ready === true", null, { timeout: 30000 });

  const total = await page.evaluate("window.TOTAL_DURATION");
  const totalFrames = Math.round(total * FPS);
  console.log(`rendering ${totalFrames} frames @ ${FPS}fps (${total.toFixed(1)}s) → ${framesDir}`);

  const t0 = Date.now();
  for (let f = 0; f < totalFrames; f++) {
    await page.evaluate((t) => window.seek(t), f / FPS);
    const name = `frame_${String(f + 1).padStart(5, "0")}.png`;
    await page.screenshot({ path: path.join(framesDir, name), clip: { x: 0, y: 0, width: 1920, height: 1080 } });
    if (f % 120 === 0 || f === totalFrames - 1) {
      const pct = (((f + 1) / totalFrames) * 100).toFixed(1);
      const elapsed = ((Date.now() - t0) / 1000).toFixed(0);
      console.log(`  ${f + 1}/${totalFrames} (${pct}%)  ${elapsed}s`);
    }
  }

  await browser.close();
  server.close();
  console.log(`done in ${((Date.now() - t0) / 1000).toFixed(0)}s`);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
