# Agent constitution

Rules for AI agents working in this repository — Claude Code, Codex, Cursor and
anything else that commits here. Short by design: it holds the conventions an
agent cannot infer from the code, and nothing that the code already says.

## Language

**Commit messages, pull request titles and pull request descriptions are
English. Always, with no exceptions.**

This is the project's own convention — `git log` on `master` is in English —
and it is the one that serves the reader. A commit message is read by whoever
runs `git blame` on a line years from now: a contributor, a packager, a person
bisecting a regression. They may share no language with the author, and unlike
a conversation, they cannot ask.

**Build tooling is English too, and so is everything it prints.** Build and
packaging scripts, CI workflows, the names of their steps, their error messages
and their output: an agent writes all of it in English. This text lands in CI
logs and in the Actions interface, where it is read by whoever opens a failed
run — not by whoever wrote it.

**Everything else follows the file it is in.** Parts of `docs/` and many code
comments are Ukrainian. That is deliberate: they are written for the people who
maintain this fork. Match the language of the file you are editing, and never
translate an existing file because it is not in English — a translation pass
nobody asked for buries the real change in the diff.

**Conversation with the user is in whatever language the user writes in.**

## Comments

An agent does not add comments to the code it writes here. Name things so that
the code reads without them; when something genuinely cannot be inferred, it
belongs in `docs/` or in the commit message, where it is read on purpose rather
than skimmed past.

This rule covers what an agent adds. Comments already in the tree stay: removing
somebody else's explanation is as unwelcome as adding an unasked-for one.

## Scope

New repository-wide rules for agents belong in this file. Rules about the
project format and about editing models in files are in
[docs/AGENT_GUIDE.md](docs/AGENT_GUIDE.md) — that is a different document, and
it governs data, not workflow.

`CLAUDE.md` is a pointer to this file, so that Claude Code picks it up
automatically; the text lives here, once.
