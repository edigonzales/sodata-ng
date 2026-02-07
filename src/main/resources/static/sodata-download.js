(() => {
  "use strict";

  const COPIED_CLASS = "is-copied";
  const COPIED_DURATION_MS = 1800;
  const copiedTimers = new WeakMap();

  async function copyToClipboard(text) {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
      return;
    }

    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.setAttribute("readonly", "");
    textarea.style.position = "absolute";
    textarea.style.left = "-9999px";

    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand("copy");
    document.body.removeChild(textarea);
  }

  function showCopiedState(button) {
    const activeTimer = copiedTimers.get(button);
    if (activeTimer) {
      window.clearTimeout(activeTimer);
    }

    button.classList.add(COPIED_CLASS);
    const nextTimer = window.setTimeout(() => {
      button.classList.remove(COPIED_CLASS);
      copiedTimers.delete(button);
    }, COPIED_DURATION_MS);
    copiedTimers.set(button, nextTimer);
  }

  document.addEventListener("change", async (event) => {
    const target = event.target;
    if (!(target instanceof HTMLSelectElement) || !target.classList.contains("download-select")) {
      return;
    }

    const selectedOption = target.options[target.selectedIndex];
    const selectedValue = selectedOption ? selectedOption.value.trim() : "";
    if (selectedValue.length === 0) {
      return;
    }

    const mode = target.dataset.downloadMode;

    if (mode === "clipboard") {
      try {
        await copyToClipboard(selectedValue);
      } catch (error) {
        // Ignore clipboard errors to keep UI unobtrusive.
      }
    } else if (mode === "download" || mode === "subunit") {
      window.open(selectedValue, "_blank", "noopener");
    }

    target.selectedIndex = 0;
  });

  document.addEventListener("click", async (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) {
      return;
    }

    const copyButton = target.closest(".download-badge-copy");
    if (!(copyButton instanceof HTMLButtonElement)) {
      return;
    }

    const copyUrl = copyButton.dataset.copyUrl;
    if (!copyUrl || copyUrl.trim().length === 0) {
      return;
    }

    try {
      await copyToClipboard(copyUrl.trim());
      showCopiedState(copyButton);
    } catch (error) {
      // Ignore clipboard errors to keep UI unobtrusive.
    }
  });
})();
