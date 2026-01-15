(() => {
  const host = document.querySelector("[data-ajax-area]");
  if (!host) return;

  const areaId = host.dataset.areaId;
  const linkSelector = host.dataset.linkSelector;
  const formSelector = host.dataset.formSelector;

  async function load(url, push = true) {
    const res = await fetch(url, { headers: { "X-Requested-With": "XMLHttpRequest" } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);

    const html = await res.text();
    const doc = new DOMParser().parseFromString(html, "text/html");

    const nextArea = doc.getElementById(areaId);
    const curArea = document.getElementById(areaId);
    if (!nextArea || !curArea) throw new Error(`#${areaId} not found`);

    curArea.replaceWith(nextArea);
    if (push) history.pushState({}, "", url);
  }

  document.addEventListener("click", (e) => {
    const a = e.target.closest(linkSelector);
    if (!a) return;

    if (a.classList.contains("disabled") || a.classList.contains("active")) {
      e.preventDefault();
      return;
    }

    e.preventDefault();
    load(a.href).catch(console.error);
  });

  if (formSelector) {
    const form = document.querySelector(formSelector);
    if (form) {
      form.addEventListener("submit", (e) => {
        e.preventDefault();
        const url = new URL(form.action, location.origin);
        url.search = new URLSearchParams(new FormData(form)).toString();
        load(url.toString()).catch(console.error);
      });
    }
  }

  window.addEventListener("popstate", () => load(location.href, false).catch(console.error));
})();
