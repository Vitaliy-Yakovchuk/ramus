# AI assistant for Ramus — experimental

The local client has an experimental Swing assistant for generating IDEF0 plans.
It supports DeepSeek and local OpenAI-compatible chat endpoints, such as Ollama.
The user must confirm the proposed plan before the application changes a model.

## Run

From the repository root, with a supported JDK installed:

```sh
./gradlew :local-client:runLocal
```

Open **Tools / Сервис → AI Assistant**, choose a provider, specify the model and
write a task. Select **Сгенерировать**, inspect the proposed plan and confirm
only if it is acceptable. Save a copy of the project first: generation may
reuse and reposition same-named blocks and replace same-named arrows in the
affected diagram, and failures are not guaranteed to roll back atomically.

### macOS on Apple Silicon

Use an ARM64 JDK 21 (for example, a Temurin distribution) and verify it with
`java -version`. After cloning the repository, run `./gradlew :local-client:test`
and `./gradlew :local-client:runLocal` from its root. The Gradle wrapper may
download Gradle and dependencies on the first build. These commands build from
source; no prebuilt app or AI service is required for the client itself.

To build a local macOS DMG, use `./gradlew :local-client:macDmg` with a full JDK
that includes `jpackage` and `jlink`. A locally built unsigned app may require
the user to approve opening it in macOS security settings. Package creation and
signing are separate from source compilation; see the root README for details.

For DeepSeek enter an API key in the password field; it is held in memory for
the session and is not intentionally written to the project. Prompts and a
snapshot containing non-system qualifier and element names are sent to the
selected cloud provider. Do not submit sensitive project data without approval.
Model availability and API billing depend on the provider and account.

For a local model, start an OpenAI-compatible service separately, select
**Локальная LLM**, enter its model name and use a loopback HTTP endpoint such
as `http://127.0.0.1:11434/v1`. The model must already be installed. Remote
hosts and URLs containing credentials are rejected. Local generation depends
on the selected model returning the supported JSON operation format.

The older `:ramus-ai-agent:run` starts a separate rule-based analysis service;
it is not an LLM provider and does not generate executable plans. It is not
required for the DeepSeek or local LLM options in the assistant window.

## Lab 1 IDEF0 assignment profile

Both providers receive the same `Lab1Specification`: one context block on A-0,
3–6 main functions on A0 and at least one 3–6-function second-level diagram.
For a task with three stated stages, those stages belong on A0 and their details
belong on A1/A2/A3, not on an overloaded flat canvas.

`Lab1PlanValidator` validates the complete plan before the first Engine write:
local references, ICOM sides, outputs/controls/mechanisms, hierarchical paths,
feedback and boundary balance. Invalid AI responses get one repair request;
if still invalid, generation stops without applying the plan. Stream names and
directions identify inherited interfaces. The executor connects those interfaces
with native crosspoints instead of creating unrelated same-named boundary stubs.
Local details must explicitly specify `tunnel: "soft"` and a rationale.

An inherited boundary branch may use a more specific visible `name`. Set its
`parentStream` to the exact visible name at the immediately enclosing parent;
ICOM must remain unchanged. For example, the handout's control branch
`Правила сборки и тестирования` inherits `Правила и процедуры`. This is an
explicit interface reference, not a name-similarity heuristic or a special-case
transport rule. The executor preserves the parent name and links the renamed
branch through its native crosspoint. Internal arrows and local tunnels cannot
use `parentStream`. Omitted `parentStream` retains same-name inheritance.

Use **Пример: транспортная компания** for the worked transport assignment,
without an API call. It creates A-0, A0 (three stages) and A1/A2/A3 (4/5/4
functions), with conditional storage, preparation, maintenance, security and
forwarding described on the relevant functions. This is an explicit example
button; arbitrary transport prompts are no longer silently replaced by it.

Existing incompatible drafts are preserved in their classifier; generation uses
a separate named model. A repeated compatible plan reuses functions and streams.
Function details are available in the **Описание AI** text attribute. After
generation A0 opens; go up for A-0 and open a stage for its decomposition.

The integration test produces `local-client/build/lab1-transport.rsf` and native
print previews `lab1-A-0.png`, `lab1-A0.png`, `lab1-A1.png`, `lab1-A2.png`,
`lab1-A3.png`. Generated projects are test artifacts and must not be committed.
The separate sample-computer IDEF0 and order-entry DFD tasks from the handout
are not automatically completed by generating the student's transport variant.

## Current limitations

- The supported write operations are limited to creating functions and streams.
- Structural validation cannot prove the semantic accuracy or completeness of
  every arbitrary business variant. Inspect the generated plan and descriptions.
- Automatic placement attempts to avoid blocks and labels, but dense diagrams,
  inherited boundary arrows and tilda leaders can still overlap or appear
  disconnected. Inspect and adjust the result in Ramus.
- AI output is untrusted; review it and preserve backups. The snapshot is not a
  full semantic export of the project, and the operation parser is limited.

## Tests

```sh
./gradlew :local-client:test
```

Tests cover routing and occupied lanes, local HTTP integration with a mock
service, domain-independent validation, native boundary inheritance, repeated
generation and all five transport diagrams saved and reopened using Ramus.
They do not establish visual quality for every
project or verify a real cloud/local model.

Before publishing, review the complete diff, third-party licenses, bundled
resources, local paths and secrets. Do not commit API keys, project snapshots,
personal projects, `.env` files or local Gradle caches.
