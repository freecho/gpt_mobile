# Safer Image Upload Handling Design

## Goal
Reduce chat attachment OOM risk without unnecessarily degrading image quality.

## Requirements
- Enforce a conservative global file upload cap of 50 MB.
- Only resize images when they exceed a dimension threshold.
- Keep normal-sized images at original size.
- Start validation, resize, and base64 preparation immediately after attachment.
- If the user taps Send before preprocessing finishes, keep the message in a sending/loading state until preprocessing completes.
- Notify users when an oversized image will be resized.
- Support edit-send and retry flows without reintroducing repeated heavy work on the UI thread.

## Recommended Architecture
Introduce an attachment preprocessing pipeline in chat draft state.

### Draft attachment state
Replace the current `selectedFiles: List<String>` draft state with attachment models that track:
- source file path
- prepared upload file path
- mime type
- file size
- image dimensions if applicable
- whether resize is required
- preprocessing status (`Preparing`, `Ready`, `Failed`)
- optional user-facing notice
- prepared encoded payload for immediate send reuse

### Preprocessing on attach
When a file is attached:
1. Copy it into the app attachments directory.
2. Validate file size against the global 50 MB cap.
3. Inspect image mime type and dimensions.
4. If image dimensions exceed the configured threshold, create a resized upload-ready file and notify the user that the image will be resized.
5. If dimensions are within threshold, keep the original copied file as the upload-ready file.
6. Base64-encode the upload-ready file on a background dispatcher and store the prepared payload in draft state.

### Sending behavior
When Send is tapped:
- if no attachments are still preparing, send immediately
- if any attachments are still preparing, keep the message in a sending/loading state and delay request construction until all draft attachments become `Ready` or one fails
- if any attachment failed, keep the draft blocked until the failed attachment is removed or retried

### Message persistence and reuse
Persist prepared upload file paths into `MessageV2.files` so subsequent requests operate on upload-ready files instead of original oversized sources.

For reuse:
- immediate send uses prepared payloads from draft state
- edit-send of a message keeps its stored prepared file paths
- retry uses cached/prepared payloads when available and otherwise rebuilds from the stored upload-ready file on a background dispatcher

## Repository / request construction
Repository request builders should no longer perform resize decisions. They should:
- prefer prepared encoded payloads when available
- otherwise encode from already-prepared file paths on `Dispatchers.IO`
- avoid duplicated MIME lookups and fail clearly if a message ends up with no encodable content

## User experience
- Attached files appear immediately.
- Preparing attachments show a subtle loading state.
- Oversized images show a notice that they will be resized before upload.
- Oversized files above 50 MB are rejected immediately with an error message.
- Send remains responsive; the chat just appears to be sending while preprocessing completes.

## Testing
Add coverage for:
- rejecting files above 50 MB
- keeping under-threshold images at original size
- marking oversized images for resize
- waiting for preprocessing before sending
- retry/edit-send reusing prepared upload data or paths
- failure state blocking send until resolved
