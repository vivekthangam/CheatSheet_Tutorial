[🏠 Back to Home](README.md) | [📚 Doc Generation Master Guide](doc_generation_master_guide.md) | [⚡ VitePress Guide](vitepress_master_guide.md) | [🐍 MkDocs Guide](mkdocs_material_master_guide.md)

# 🦖 Docusaurus: Enterprise React Documentation & Multi-Versioning Master Guide

[![Docusaurus](https://img.shields.io/badge/Docusaurus-3.3%2B-green.svg?style=for-the-badge&logo=docusaurus)](https://docusaurus.io/)
[![React](https://img.shields.io/badge/React-18%2B-blue.svg?style=for-the-badge&logo=react)](https://react.dev/)
[![Search](https://img.shields.io/badge/Search-Algolia%20%7C%20Local-yellow.svg?style=for-the-badge)](https://github.com/easyops-cn/docusaurus-search-local)

An exhaustive guide to building enterprise-grade documentation portals with **Docusaurus**, Meta's battle-tested framework for versioned technical documentation.

---

## 📑 Table of Contents

- [📑 Generated Documentation Hub Index](#-generated-documentation-hub-index)
- [1. When to Choose Docusaurus: The Enterprise Value Proposition](#1-when-to-choose-docusaurus-the-enterprise-value-proposition)
- [2. Complete Production Configuration (`docusaurus.config.js`)](#2-complete-production-configuration-docusaurusconfigjs)
- [3. Hierarchical Sidebar Management (`sidebars.js`)](#3-hierarchical-sidebar-management-sidebarsjs)
- [4. Local Search vs Algolia DocSearch](#4-local-search-vs-algolia-docsearch)
- [5. MDX Interactive React Components in Markdown](#5-mdx-interactive-react-components-in-markdown)
- [6. Automated GitHub Actions Deployment](#6-automated-github-actions-deployment)
- [7. Docusaurus Production Gotchas & Webpack Optimization](#7-docusaurus-production-gotchas--webpack-optimization)

---

## 📑 Generated Documentation Hub Index

| Technology / Scope | Master Guide Link | Runtime Engine | Offline Search System | Best For |
| :--- | :--- | :--- | :--- | :--- |
| **Architectural Overview & Master Matrix** | **[doc_generation_master_guide.md](doc_generation_master_guide.md)** | Polyglot (SSG / Islands / SPAs) | Inverted Index vs Chunks vs Algolia | Architectural comparison, trade-offs, and decision flowchart |
| **⚡ VitePress** | **[vitepress_master_guide.md](vitepress_master_guide.md)** | Vite + Vue 3 SPA | Built-in `MiniSearch` (Offline) | Modern developer portals, sleek aesthetics, sub-second HMR |
| **🐍 Material for MkDocs** | **[mkdocs_material_master_guide.md](mkdocs_material_master_guide.md)** | Python 3 (Zero Node.js) | Built-in `Lunr.js` in Web Workers | Pure Markdown repositories with zero frontend complexity |
| **🚀 Hugo & Hextra** | **[hugo_master_guide.md](hugo_master_guide.md)** | Go (Standalone Binary) | Built-in `FlexSearch` (Sub-ms) | Extreme build speed (thousands of pages in milliseconds) |
| **🌟 Starlight (Astro)** | **[starlight_astro_master_guide.md](starlight_astro_master_guide.md)** | Astro (Component Islands) | Built-in `Pagefind` (Segmented Chunks)| Zero client-side JS by default, massive documentation sites |
| **🦖 Docusaurus** | **[docusaurus_master_guide.md](docusaurus_master_guide.md)** | Webpack + React | Local Search Plugin / Algolia | Enterprise projects needing multi-versioning & interactive MDX |
| **📄 Docsify** | **[docsify_master_guide.md](docsify_master_guide.md)** | Vanilla JS (Runtime SPA) | Built-in Search with LocalStorage | Instant live preview & GitHub Pages with zero compilation steps |

---

# 1. When to Choose Docusaurus: The Enterprise Value Proposition

Docusaurus is the industry standard for large enterprise open-source projects (React Native, Jest, Redux, Supabase, Babel).
- **First-Class Multi-Versioning:** Allows maintaining documentation for multiple releases simultaneously (`v1.0`, `v2.0`, `v3.0`). Running `npm run docusaurus docs:version 2.0.0` automatically freezes and archives a versioned snapshot.
- **MDX Support:** Allows embedding live, interactive React components directly inside Markdown files.
- **Internationalization (i18n):** Native support for translating documentation into 20+ languages out of the box.

---

# 2. Complete Production Configuration (`docusaurus.config.js`)

Save this file as `docusaurus.config.js`:

```javascript
// @ts-check
const config = {
  title: 'CheatSheet Tutorial Hub',
  tagline: 'Enterprise Systems, Cloud Native & Polyglot Architecture Master Guides',
  favicon: 'img/favicon.ico',

  url: 'https://vivekthangam.github.io',
  baseUrl: '/CheatSheet_Tutorial/',

  organizationName: 'vivekthangam',
  projectName: 'CheatSheet_Tutorial',
  trailingSlash: false,

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  presets: [
    [
      'classic',
      ({
        docs: {
          sidebarPath: './sidebars.js',
          routeBasePath: '/', // Serves docs from root URL
          editUrl: 'https://github.com/vivekthangam/CheatSheet_Tutorial/tree/main/',
        },
        blog: false, // Disables blog if strictly building docs
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  // LOCAL OFFLINE SEARCH PLUGIN (No Algolia required!)
  themes: [
    [
      require.resolve('@easyops-cn/docusaurus-search-local'),
      ({
        hashed: true,
        language: ['en'],
        highlightSearchTermsOnTargetPage: true,
        explicitSearchResultPath: true,
        indexDocs: true,
        docsRouteBasePath: '/',
      }),
    ],
  ],

  themeConfig: {
    navbar: {
      title: 'Engineering Hub',
      logo: {
        alt: 'Logo',
        src: 'img/logo.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'systemsSidebar',
          position: 'left',
          label: 'Systems (Rust & Go)',
        },
        {
          type: 'docSidebar',
          sidebarId: 'cloudSidebar',
          position: 'left',
          label: 'Cloud & DevOps',
        },
        {
          href: 'https://github.com/vivekthangam/CheatSheet_Tutorial',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      copyright: `Copyright © ${new Date().getFullYear()} Vivek Thangam. Built with Docusaurus.`,
    },
  },
};

module.exports = config;
```

---

# 3. Hierarchical Sidebar Management (`sidebars.js`)

Save this file as `sidebars.js`:

```javascript
/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  systemsSidebar: [
    {
      type: 'category',
      label: '🦀 Systems: Rust',
      items: [
        'rust_master_guide',
        'rust_technical_terms_master_guide',
        'rust_scenarios_master_guide',
      ],
    },
    {
      type: 'category',
      label: '🐹 Systems: Golang',
      items: [
        'golang_master_guide',
        'golang_technical_terms_master_guide',
        'golang_scenarios_master_guide',
      ],
    },
  ],
  cloudSidebar: [
    {
      type: 'category',
      label: '☁️ Cloud Native & DevOps',
      items: [
        'docker_master_guide',
        'kubernetes_master_guide',
        'devops_iac_technical_terms_master_guide',
        'devops_iac_scenarios_master_guide',
      ],
    },
  ],
};

module.exports = sidebars;
```

---

# 4. Local Search vs Algolia DocSearch

- **Algolia DocSearch:** Meta's default choice. A hosted crawler scrapes your public domain weekly. If you are behind a corporate VPN or local intranet, Algolia will fail to index your site.
- **`@easyops-cn/docusaurus-search-local`:** The recommended offline solution. It builds an in-memory index using Lunr during the `docusaurus build` command, allowing fast, offline search with zero cloud dependencies.

---

# 5. MDX Interactive React Components in Markdown

With Docusaurus, you can embed live interactive React state inside Markdown:

```mdx
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Interactive Multi-Platform Guide

<Tabs>
  <TabItem value="rust" label="Rust" default>
    ```rust
    println!("Hello from Rust!");
    ```
  </TabItem>
  <TabItem value="go" label="Golang">
    ```go
    fmt.Println("Hello from Go!")
    ```
  </TabItem>
</Tabs>
```

---

# 6. Automated GitHub Actions Deployment

Create `.github/workflows/deploy-docusaurus.yml`:

```yaml
name: Deploy Docusaurus to GitHub Pages

on:
  push:
    branches: [main]
  workflow_dispatch:

permissions:
  contents: write

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: actions/setup-node@v4
        with:
          node-version: 20
          cache: npm

      - name: Install dependencies
        run: npm install

      - name: Build and Deploy to GitHub Pages
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          git config --global user.name "github-actions[bot]"
          git config --global user.email "github-actions[bot]@users.noreply.github.com"
          npm run deploy
```

---

# 7. Docusaurus Production Gotchas & Webpack Optimization

1. **Node Memory Exhaution on Large Repos:** If your documentation contains hundreds of massive markdown files, Webpack can hit Node's default 2GB memory limit during build.
   - *Fix:* Increase heap limit in `package.json`:
     `"build": "NODE_OPTIONS=--max-old-space-size=4096 docusaurus build"`
2. **React Hydration Mismatches:** Occurs if you write unclosed HTML tags inside standard Markdown. Always ensure custom HTML tags are self-closing (`<br />`, `<img />`).

---
[🏠 Back to Home](README.md) | [📚 Doc Generation Master Guide](doc_generation_master_guide.md) | [⚡ VitePress Guide](vitepress_master_guide.md)
