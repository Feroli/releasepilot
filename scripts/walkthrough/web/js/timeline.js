// Ordered scene registry. Start times are derived from durations so the caption
// track in captions.js stays aligned with these beats.

import { intro } from "./scenes/intro.js";
import { problem } from "./scenes/problem.js";
import { aggregate } from "./scenes/aggregate.js";
import { ddd } from "./scenes/ddd.js";
import { stateMachine } from "./scenes/stateMachine.js";
import { invariants } from "./scenes/invariants.js";
import { hexagon } from "./scenes/hexagon.js";
import { cqrs } from "./scenes/cqrs.js";
import { commandFlow } from "./scenes/commandFlow.js";
import { asyncFlow } from "./scenes/asyncFlow.js";
import { persistence } from "./scenes/persistence.js";
import { ports } from "./scenes/ports.js";
import { errors } from "./scenes/errors.js";
import { apiDemo } from "./scenes/apiDemo.js";
import { tests } from "./scenes/tests.js";
import { tradeoffs } from "./scenes/tradeoffs.js";
import { next } from "./scenes/next.js";
import { outro } from "./scenes/outro.js";

export const SCENES = [
  intro(),
  problem(),
  aggregate(),
  ddd(),
  stateMachine(),
  invariants(),
  hexagon(),
  cqrs(),
  commandFlow(),
  asyncFlow(),
  persistence(),
  ports(),
  errors(),
  apiDemo(),
  tests(),
  tradeoffs(),
  next(),
  outro(),
];

let acc = 0;
for (const s of SCENES) {
  s.start = acc;
  acc += s.dur;
}

export const TOTAL = acc;
