# Implementation Plan: File Attachments & Dark Mode Email Client Simulator

## 1. Overview & Objectives
Based on your feedback, we will implement two high-impact features in **MailCraft**:
1. **Real File Attachments for PDFs & Device Media**:
   - Zero-permission modern Android document/media picker (`ActivityResultContracts.OpenMultipleDocuments`).
   - Full RFC-compliant MIME packaging (`multipart/mixed` containing a `multipart/alternative` body + Base64 attachment parts).
   - Attachment tray UI in both the **Visual Composer** and **Deploy & Send Screen**: displaying filename, formatted file size (e.g., `1.4 MB`), MIME badge, and one-tap remove button.
   - Guardrails against Gmail's 25MB total payload limit with real-time payload size calculations and warning alerts.

2. **Dark Mode Email Client Rendering Simulator**:
   - Live client appearance switcher (☀️ Light Mode vs 🌙 Dark Mode Client Simulation) in both **Compose Preview** and **HTML Workstation Screen**.
   - Simulates how major email clients (Gmail, Apple Mail, Outlook) automatically invert background colors, preserve brand accent colors, and adjust dark contrast.
   - Emulates `@media (prefers-color-scheme: dark)` and `[data-ogsc]` rules inside the embedded WebView preview and visual cards.
   - Gives creators instant confidence that their emails remain readable and beautiful in dark mode before dispatch.

---

## 2. Proposed Architecture & Changes

### A. Data Models & Attachment Representation
- **`com.example.model.EmailAttachment`**:
  ```kotlin
  data class EmailAttachment(
      val id: String = UUID.randomUUID().toString(),
      val uriString: String,
      val fileName: String,
      val mimeType: String,
      val sizeBytes: Long
  )
  ```
- **`com.example.model.EmailMessage`**:
  - Add `attachments: List<EmailAttachment> = emptyList()` to the core message model.
  - Store attachment counts and names in `SentEmailEntity` for historical records.

### B. SMTP Engine (`GmailSmtpClient.kt`)
- Transition from single `multipart/alternative` to nested RFC 2046 MIME structure when attachments exist:
  ```text
  Content-Type: multipart/mixed; boundary="MIXED_BOUNDARY"

  --MIXED_BOUNDARY
  Content-Type: multipart/alternative; boundary="ALT_BOUNDARY"

  --ALT_BOUNDARY
  Content-Type: text/plain; charset=UTF-8
  ...
  --ALT_BOUNDARY
  Content-Type: text/html; charset=UTF-8
  ...
  --ALT_BOUNDARY--

  --MIXED_BOUNDARY
  Content-Type: application/pdf; name="Document.pdf"
  Content-Disposition: attachment; filename="Document.pdf"
  Content-Transfer-Encoding: base64

  <Base64 chunks (76 chars/line)>
  --MIXED_BOUNDARY--
  ```
- Read bytes efficiently via Android `ContentResolver.openInputStream(Uri)` with memory-conscious streaming/buffering.
- Provide detailed progress logs in the SMTP terminal (`"Encoding attachment: Proposal.pdf (1.2 MB)..."`).

### C. UI & Interaction: Attachment Tray
- **Composer & Deploy Screens**:
  - "Attach Files" button with paperclip icon.
  - Attachment chip row / cards showing file type icon (PDF, Image, Document), file name, size string, and delete button.
  - Total payload counter (e.g. `Attachments: 2 files (2.4 MB / 25 MB max)`).
  - Validation: warns if attachments exceed 25MB before attempting SMTP dispatch.

### D. Dark Mode Email Client Simulator
- **Preview Toggle**:
  - Add dual-state pill button `[ ☀️ Light Client | 🌙 Dark Client ]` in:
    1. Visual template preview modal in `ComposeScreen.kt`.
    2. Interactive WebView preview dialog in `HtmlWorkstationScreen.kt`.
- **Simulation Engine**:
  - **CSS Dark Mode Injection**: Injects client-style dark inversion styling into the WebView preview:
    - Background forced to `#1E1E24` or `#121216` (matching Gmail/Apple Mail dark schemes).
    - Email card containers adapted with proper contrast.
    - Text auto-adjusted for readability.
    - Custom dark theme testing CSS (`@media (prefers-color-scheme: dark)`).
  - **Visual Indicator**: Client warning badges if text contrast falls below standard readability levels when rendered in dark mode.

---

## 3. Verification & Quality Assurance
- **Local Unit & Robolectric Tests**:
  - Test nested MIME boundary generation in `GmailSmtpClient`.
  - Test attachment byte calculation and Base64 wrapping.
  - Test variable interpolation alongside attachments.
- **Compilation Check**:
  - Run `compile_applet` and `:app:testDebugUnitTest` to guarantee 0 build or test failures.
- **Design Consistency**:
  - Adhere strictly to the Purple-on-White palette (`#7C3AED`) with touch targets ≥ 48dp.
