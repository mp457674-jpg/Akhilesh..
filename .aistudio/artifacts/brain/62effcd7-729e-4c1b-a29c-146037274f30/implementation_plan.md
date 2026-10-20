# Jarvis: Live Mobile Command System & Personal Assistant

Immediate launch specification for the full-featured Jarvis Android assistant, matching the user's uploaded reference interface (`IMG_1879.webp`) with working interactive panels for every component on screen.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Immediate Execution Scope (All Components Active & Openable):**
> 1. **Header Navigation**: Top capsule pill toggle `[ ⊞ DASHBOARD | 🗨 CHAT ]` with drawer icon.
> 2. **AI Agents & Workflows Widget**:
>    - `Daily Planner` (Tap to view and schedule tasks).
>    - `Morning Brief` (Tap to listen to daily weather, agenda, and news brief).
>    - `Market Watch` (Tap to view financial/crypto/tech tickers).
> 3. **System Online Status Widget**:
>    - "Your Phone is Online" with live Battery %, Wi-Fi connection state, Free RAM, and Storage.
> 4. **Orbital Particle Core & HUD Nodes**:
>    - Animated 3D rotating planetary sphere with glowing cyan particle clouds and audio amplitude reactivity.
>    - Action pill: `[ • START AI ]` / `[ • STOP ]` to trigger real-time voice command listening.
>    - Connected circuit nodes:
>      - `[ MEMORY 41 ]` (Tap opens stored reminders, notes, and memory database).
>      - `[ SOUL ]` (Tap opens AI persona, voice pitch, speed, and language settings).
>      - `[ SETTING ]` (Tap opens hardware overrides: Volume, Wi-Fi, Torch, Bluetooth).
> 5. **Today Headlines & Tasks**:
>    - Left: Live tech and world headlines with neon border accents.
>    - Right: Interactive `Today Tasks` (shows remaining count, progress bar, and checkable task circles synced with Room database & Android notifications).
> 6. **Voice Commands & Phone Control**:
>    - *"Call [Contact Name]"* -> Queries device contacts and dials immediately.
>    - *"Message [Contact Name] [Text]"* -> Opens WhatsApp or SMS with pre-filled text.
>    - *"Open [App Name]"* -> Launches installed applications.
>    - *"Set volume to 80%"* -> Adjusts device audio stream.
>    - *"Turn on flashlight"* -> Toggles camera torch.
> 7. **Permissions Setup**:
>    - One-tap permission grant card for Contacts, Microphone, Camera, and Notifications.

---

## 1. Overview & Core Concept

Transform the user's Android phone into a futuristic, autonomous Jarvis command center that looks and operates like the uploaded mobile screenshot. Every card, node, and button is fully interactive and functional.

---

## 2. Technical Architecture & File Plan

- `app/src/main/java/com/example/ui/screens/DashboardScreen.kt`: The main dashboard UI matching `IMG_1879.webp` with grid background, workflow cards, orbital particle canvas, HUD circuit nodes, and today tasks.
- `app/src/main/java/com/example/ui/components/OrbitalGlobeCanvas.kt`: High-performance custom Canvas rendering the 3D rotating particle globe with orbit ellipses and glow.
- `app/src/main/java/com/example/system/DeviceController.kt`: Contacts querying (`ContactsContract`), dialing, WhatsApp/SMS, volume control (`AudioManager`), and flashlight.
- `app/src/main/java/com/example/system/JarvisBrain.kt`: Fast NLP engine matching calls, messages, reminders, settings, and apps.
- `app/src/main/java/com/example/system/JarvisSpeechManager.kt`: Real-time speech-to-text with interim streaming and bilingual text-to-speech.
- `app/src/main/java/com/example/MainActivity.kt`: Top `[ DASHBOARD | CHAT ]` toggle, permission launcher, and state wiring.

---

## 3. Verification & Build Confirmation

1. Verify Kotlin compilation via `compile_applet`.
2. Verify all unit tests pass with `gradle :app:testDebugUnitTest`.
3. Validate layout rendering and touch targets on emulator.
