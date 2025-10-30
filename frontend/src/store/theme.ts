import { defineStore } from 'pinia';

export type ThemeMode = 'light' | 'dark';

const STORAGE_KEY = 'ccm-theme-mode';

export const useThemeStore = defineStore('theme', {
  state: () => ({
    mode: 'light' as ThemeMode,
    initialised: false,
  }),
  actions: {
    initialise() {
      if (this.initialised) return;
      const stored = localStorage.getItem(STORAGE_KEY) as ThemeMode | null;
      if (stored === 'light' || stored === 'dark') {
        this.applyTheme(stored, false);
      } else {
        const prefersDark =
          window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        this.applyTheme(prefersDark ? 'dark' : 'light', false);
      }

      const media = window.matchMedia('(prefers-color-scheme: dark)');
      const handler = (event: MediaQueryListEvent) => {
        if (!localStorage.getItem(STORAGE_KEY)) {
          this.applyTheme(event.matches ? 'dark' : 'light', false);
        }
      };
      if (typeof media.addEventListener === 'function') {
        media.addEventListener('change', handler);
      } else if (typeof media.addListener === 'function') {
        media.addListener(handler);
      }

      this.initialised = true;
    },
    toggleTheme() {
      this.applyTheme(this.mode === 'dark' ? 'light' : 'dark');
    },
    applyTheme(mode: ThemeMode, persist = true) {
      this.mode = mode;
      document.documentElement.classList.toggle('dark', mode === 'dark');
      if (persist) {
        localStorage.setItem(STORAGE_KEY, mode);
      }
    },
    resetPreference() {
      localStorage.removeItem(STORAGE_KEY);
    },
  },
});
