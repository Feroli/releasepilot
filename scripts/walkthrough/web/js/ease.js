// Pure, deterministic timing helpers. No wall-clock, no randomness at runtime.

export const clamp = (v, lo = 0, hi = 1) => Math.min(hi, Math.max(lo, v));

export const lerp = (a, b, t) => a + (b - a) * t;

// map value x from [inMin,inMax] onto [outMin,outMax], clamped, with optional easing
export function mapRange(x, inMin, inMax, outMin, outMax, easeFn = (t) => t) {
  if (inMax === inMin) {
    return outMin;
  }
  const t = clamp((x - inMin) / (inMax - inMin));
  return lerp(outMin, outMax, easeFn(t));
}

export const easeOutCubic = (t) => 1 - Math.pow(1 - t, 3);
export const easeInCubic = (t) => t * t * t;
export const easeInOutCubic = (t) =>
  t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
export const easeOutQuint = (t) => 1 - Math.pow(1 - t, 5);
export const easeOutExpo = (t) => (t >= 1 ? 1 : 1 - Math.pow(2, -10 * t));

// overshoot / spring-like settle for "pop in" motion
export function easeOutBack(t, s = 1.7) {
  const c1 = s;
  const c3 = c1 + 1;
  return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
}

// smooth 0..1..0 pulse across a window
export const smoothstep = (a, b, x) => {
  const t = clamp((x - a) / (b - a));
  return t * t * (3 - 2 * t);
};

// deterministic pseudo-random from an integer seed (for particle layouts)
export function rand(seed) {
  const x = Math.sin(seed * 127.1 + 311.7) * 43758.5453;
  return x - Math.floor(x);
}

// staggered reveal helper: returns 0..1 progress for item i given local time
export function stagger(lt, startAt, itemGap, dur, i, easeFn = easeOutBack) {
  return clamp(easeFn(clamp((lt - startAt - i * itemGap) / dur)));
}
