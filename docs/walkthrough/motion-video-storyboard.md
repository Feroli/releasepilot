# ReleasePilot Walkthrough Motion Video Storyboard

This storyboard will become the generated challenge walkthrough video.

## Target Runtime

6 to 9 minutes, never over 10 minutes.

## Scene Plan

| Scene | Duration | Visual | Message |
| --- | --- | --- | --- |
| 1 | 30s | Title card and deployment pipeline | ReleasePilot moves versions through `dev -> staging -> production`. |
| 2 | 60s | Promotion state machine | Promotion is the aggregate and owns lifecycle transitions. |
| 3 | 60s | Invariant callouts | No skipping, previous environment completion, one active target promotion, approver-only approval, terminal immutability. |
| 4 | 60s | Hexagonal architecture diagram | Controllers and consumers enter through application ports; infrastructure stays outside the core. |
| 5 | 60s | Command processing sequence | Commands call handlers, aggregate emits events, transaction saves aggregate and outbox. |
| 6 | 60s | Async event flow | API returns before audit consumer persists event audit rows. |
| 7 | 45s | CQRS diagram and endpoint examples | Read handlers return consumer-shaped DTOs. |
| 8 | 45s | Ports and stubs | Deployment, issue tracker, notifications, and approver lookup are explicit interfaces. |
| 9 | 45s | Test pyramid/checklist | Domain, handler, API, persistence, queue, and end-to-end tests. |
| 10 | 60s | Trade-off board | Outbox over direct publish, CQRS without event sourcing, fixed pipeline, stubbed integrations. |
| 11 | 45s | Optional agent flow | Release-notes agent is a stretch tool-calling loop after approval. |
| 12 | 45s | Closing checklist | How to run, what is complete, what would improve next. |

## Generation Approach

The final video should be generated from repo-owned source:

- Mermaid diagrams from `docs/challenge-architecture.md`.
- Scene captions from `docs/walkthrough/motion-video-script.md`.
- Optional HTML/CSS animation or scripted frame generation.
- MP4 output in `deliverables/walkthrough/releasepilot-walkthrough.mp4`.
