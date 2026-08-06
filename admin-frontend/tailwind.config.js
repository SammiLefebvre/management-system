/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', '"SF Pro Text"', '"Segoe UI"', 'Roboto', 'Helvetica', 'Arial', 'sans-serif'],
      },
      borderRadius: {
        '2xl': '20px',
        'xl': '12px',
      },
      boxShadow: {
        'apple-sm': '0 2px 8px rgba(0,0,0,0.04)',
        'apple-md': '0 8px 24px rgba(0,0,0,0.06)',
        'apple-lg': '0 16px 40px rgba(0,0,0,0.08)',
      },
      transitionTimingFunction: {
        'apple': 'cubic-bezier(0.25, 0.1, 0.25, 1.0)',
      },
    },
  },
  plugins: [],
}
