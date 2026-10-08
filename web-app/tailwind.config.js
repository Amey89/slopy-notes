/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'brand': '#E53935', // Google Red like the logo
        // Define colors per specs
        'folder-completed': '#10B981', // Emerald Green
        'folder-shared': '#3B82F6', // Ocean Blue
      }
    },
  },
  plugins: [],
}