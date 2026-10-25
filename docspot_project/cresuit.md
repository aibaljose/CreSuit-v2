# CreSuit

**An AI-Assisted Visual Application Development and Native Mobile Compilation Platform**

CreSuit is a web-based platform concept for building mobile applications through a visual designer, block-based logic, and optional AI assistance. It aims to let users move from an app idea to an editable project and, where the required build infrastructure is configured, a native Android package.

> **Project status:** This document describes the intended architecture and capabilities. Mark each feature as implemented, in progress, or planned according to the current repository before using this README as a public status statement.

## Table of contents

- [Overview](#overview)
- [Goals](#goals)
- [Core modules](#core-modules)
- [Typical workflow](#typical-workflow)
- [Technology and project formats](#technology-and-project-formats)
- [AI-assisted development](#ai-assisted-development)
- [Architecture](#architecture)
- [Security considerations](#security-considerations)
- [Development status](#development-status)
- [Getting started](#getting-started)
- [Roadmap](#roadmap)
- [Acknowledgements and foundation](#acknowledgements-and-foundation)

## Overview

CreSuit combines a visual mobile UI editor, a block-based programming environment, AI-assisted generation, device testing, and a native build pipeline. Users should be able to inspect and modify generated interfaces and logic rather than being locked into a one-shot AI result.

AI is an optional assistant: the visual editor and block environment should remain useful without an AI request or an external model connection.

## Goals

- Make mobile app creation more accessible to beginners and experienced developers.
- Build editable interfaces through drag-and-drop visual components.
- Create app behavior with event-driven, block-based logic.
- Use natural-language prompts to suggest or generate UI and logic.
- Keep generated results understandable and manually editable.
- Support preview and testing on a physical Android device.
- Provide a path to export native Android builds when compilation services are configured.
- Keep credentials and build operations on trusted server-side infrastructure.

## Core modules

### 1. Studio and visual designer

A WYSIWYG workspace for composing screens with layout containers, UI controls, images, text, and other supported components.

Expected capabilities:

- Drag-and-drop component placement.
- Property inspector for text, dimensions, colors, alignment, and assets.
- Screen/component hierarchy.
- Mobile preview.
- Serialization of screen layouts into a structured schema such as `.scm`.

### 2. Visual logic and block engine

A block-based environment for defining application behavior without manually writing every line of code.

Expected logic features include:

- Component events and handlers.
- Setters and getters.
- Conditions and loops.
- Variables, lists, and procedures.
- Web/API requests and asynchronous operations.
- Validation of block connections and types.

In an App Inventor-style pipeline, visual block projects may use `.bky` files and be translated into Young Android Intermediate Language (YAIL) before compilation.

### 3. AI co-pilot

The AI layer can help translate a natural-language description into an initial UI structure and relevant block logic.

Potential capabilities:

- Prompt-to-UI generation.
- Suggestions for component properties and layout.
- Draft block-logic generation.
- Explanations of selected blocks.
- Help diagnosing missing connections or likely logic errors.
- Iterative refinement based on user instructions.

AI-generated output should be validated before it is added to the project. Users should be able to review, edit, or reject generated changes. AI output must not silently overwrite existing work.

### 4. Live companion and device preview

A companion application can connect a development studio to a physical Android device for preview and testing.

A possible design uses a pairing code or QR code over a local network. Live updates depend on the companion implementation, transport protocol, and backend support; do not describe hot reload as available until it has been verified.

### 5. Build and export pipeline

The build system is intended to transform project definitions and assets into installable Android outputs.

Possible exports, depending on the configured toolchain:

- Android Package (`.apk`)
- Android App Bundle (`.aab`)
- Portable project archive (`.aia`), if compatible with the project format

Native compilation requires a functioning build server and compatible Android build tools. Export formats and supported Android versions should be documented from actual build tests.

### 6. Authentication and project management

This module manages user sessions, projects, project metadata, and access permissions. Authentication and application-data storage are separate concerns: using Firebase Authentication does not imply that Firebase is used to store project data.

## Typical workflow

1. Create a project and add a screen.
2. Design the screen using visual components.
3. Configure component properties in the inspector.
4. Add behavior using visual blocks.
5. Optionally ask the AI co-pilot to draft or refine UI and logic.
6. Inspect and manually adjust the generated result.
7. Preview and test the app.
8. Build and export a native package when the build service is available.

## Technology and project formats

The exact stack depends on the current implementation. The following technologies are relevant to an App Inventor-style architecture:

| Area | Technology or format | Purpose |
|---|---|---|
| Visual logic | Google Blockly | Block-based programming workspace |
| Screen definitions | `.scm` | Structured screen/component definitions |
| Block definitions | `.bky` | Serialized visual logic |
| Intermediate representation | YAIL | Intermediate language in an App Inventor-style compilation flow |
| AI integration | Google Gemini API | Optional natural-language assistance |
| Native build | Android build toolchain | Package application output |
| Authentication | Firebase Authentication, if configured | User sign-in and identity |

Do not assume every item in this table is already integrated. Update it to match the code and deployment environment.

## AI-assisted development

The AI system should return structured, validated project changes rather than arbitrary text that is directly executed.

Recommended safeguards:

- Validate generated schemas and block structures.
- Sanitize and constrain model output.
- Keep a preview or diff before applying changes.
- Preserve undo/recovery options.
- Handle API failures and rate limits gracefully.
- Keep API keys on the server; never embed secret keys in browser code or mobile packages.
- Keep manual editing available when AI is unavailable.

## Architecture

A logical high-level architecture is:

```text
Web Studio
  ├── Visual Designer ──> Screen Schema (.scm)
  ├── Block Editor ─────> Block Project (.bky)
  ├── AI Co-pilot ──────> Validated UI / Logic Suggestions
  └── Project Manager
             │
             ▼
       Backend Services
         ├── Authentication and authorization
         ├── Project and asset storage
         ├── AI provider adapter
         └── Build job orchestration
                    │
                    ▼
             Build Worker
         ├── Convert / generate sources
         ├── Compile and package
         └── Return build artifacts
                    │
                    ▼
              APK / AAB
```

This is a conceptual architecture. Replace it with a repository-specific diagram once the actual services and data flows are confirmed.

## Security considerations

- Store model API keys and signing credentials in server-side secret storage.
- Authenticate and authorize every project and build request.
- Isolate build jobs and restrict their file-system access.
- Validate uploaded assets and project archives.
- Apply rate limits to AI and compilation endpoints.
- Avoid logging secrets, access tokens, or sensitive project contents.
- Treat AI output and imported project files as untrusted input.
- Define retention and deletion policies for projects, logs, and generated artifacts.

## Development status

Use this table to report only verified implementation status.

| Feature | Status | Evidence / notes |
|---|---|---|
| Visual screen designer | Update status | Link to implementation or test |
| Component property inspector | Update status | Link to implementation or test |
| Block-based logic editor | Update status | Link to implementation or test |
| AI UI generation | Update status | Provider, supported prompts, and tests |
| AI block generation | Update status | Validation and supported blocks |
| Authentication | Update status | Actual configured provider |
| Project persistence | Update status | Actual storage backend |
| Live companion | Update status | Device and network test results |
| Android compilation | Update status | Build logs and successful artifacts |
| APK/AAB export | Update status | Tested output formats |

## Getting started

Because setup varies by repository revision, use the current project instructions as the source of truth.

1. Clone the repository.
2. Install the versions of Java, Node.js, build tools, and other dependencies required by that revision.
3. Configure local environment variables using a local, untracked environment file.
4. Configure authentication and storage services, if required.
5. Configure the AI provider only if you intend to use AI features.
6. Start the frontend and backend using the repository's documented commands.
7. Run the available tests and verify a complete project save/load cycle.
8. Verify native compilation separately; a running editor does not guarantee that the build pipeline is configured.

Never commit real API keys, signing keys, passwords, or production credentials.

## Roadmap

Potential next steps:

- Improve visual editor usability and responsive previews.
- Expand the supported component and block libraries.
- Add reliable project versioning, undo, and recovery.
- Make AI changes reviewable and reversible.
- Strengthen schema validation and error reporting.
- Improve device pairing and preview reliability.
- Add reproducible build jobs and clear build logs.
- Document deployment, backup, privacy, and security procedures.
- Add automated tests for save/load, generation, and compilation.

## Acknowledgements and foundation

CreSuit draws on the broader idea of visual, block-based mobile development and an App Inventor-style workflow. The upstream foundation and the CreSuit-specific work should be described separately.

When documenting the repository, clearly identify:

- Upstream components and their licenses.
- CreSuit-specific code, UI, integrations, adaptations, and original features.
- Third-party libraries and their license obligations.
- Which capabilities are implemented versus proposed.

Do not imply that upstream code was authored by the CreSuit team, and do not reduce CreSuit's contribution to its upstream foundation alone. Review the repository's license files and notices before distributing the project.

---

## License

Add the license that actually applies to this repository. If the project contains upstream code, preserve its copyright and license notices and verify that the selected distribution terms are compatible with those obligations.
