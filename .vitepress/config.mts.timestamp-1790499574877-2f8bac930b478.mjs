// .vitepress/config.mts
import { defineConfig } from "file:///D:/project/github/1/CheatSheet_Tutorial/node_modules/vitepress/dist/node/index.js";
var config_default = defineConfig({
  title: "Enterprise Java & System Design Masterclass",
  description: "Senior & Staff Level Interview Preparation Guide",
  themeConfig: {
    // 🔍 Enable Instant Local Full-Text Search
    search: {
      provider: "local"
    },
    // 🧭 Top Navigation Links
    nav: [
      { text: "Home", link: "/" },
      { text: "Learning Path", link: "/LEARNING_PATH" },
      { text: "Main ReadMe", link: "/README" }
    ],
    // 📑 Comprehensive Sidebar Mapping Your Folders
    sidebar: [
      {
        text: "Getting Started",
        items: [
          { text: "\u{1F4D6} Master ReadMe", link: "/README" },
          { text: "\u{1F5FA}\uFE0F Learning Path", link: "/LEARNING_PATH" },
          { text: "\u{1F4CB} Categorized Files", link: "/all_markdown_files_categorized" }
        ]
      },
      {
        text: "Java Core & Concurrency",
        collapsed: false,
        items: [
          { text: "\u2615 Java Core Guide", link: "/java-core/" },
          // Point to files inside /java-core if mapped
          { text: "\u{1F4DA} Collections Reference", link: "/topics/java_collections_mastery" }
        ]
      },
      {
        text: "Frameworks & Architecture",
        collapsed: false,
        items: [
          { text: "\u{1F343} Spring Master Guide", link: "/spring-framework/" },
          { text: "\u{1F4E6} Jackson Serialization", link: "/topics/jackson_master_guide" }
        ]
      },
      {
        text: "Scenarios & Practice",
        collapsed: false,
        items: [
          { text: "\u{1F525} Master Scenarios", link: "/scenarios/" }
        ]
      }
    ],
    socialLinks: [
      { icon: "github", link: "https://github.com/" }
    ]
  }
});
export {
  config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsiLnZpdGVwcmVzcy9jb25maWcubXRzIl0sCiAgInNvdXJjZXNDb250ZW50IjogWyJjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfZGlybmFtZSA9IFwiRDpcXFxccHJvamVjdFxcXFxnaXRodWJcXFxcMVxcXFxDaGVhdFNoZWV0X1R1dG9yaWFsXFxcXC52aXRlcHJlc3NcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfZmlsZW5hbWUgPSBcIkQ6XFxcXHByb2plY3RcXFxcZ2l0aHViXFxcXDFcXFxcQ2hlYXRTaGVldF9UdXRvcmlhbFxcXFwudml0ZXByZXNzXFxcXGNvbmZpZy5tdHNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL0Q6L3Byb2plY3QvZ2l0aHViLzEvQ2hlYXRTaGVldF9UdXRvcmlhbC8udml0ZXByZXNzL2NvbmZpZy5tdHNcIjtpbXBvcnQgeyBkZWZpbmVDb25maWcgfSBmcm9tICd2aXRlcHJlc3MnXG5cbmV4cG9ydCBkZWZhdWx0IGRlZmluZUNvbmZpZyh7XG4gIHRpdGxlOiBcIkVudGVycHJpc2UgSmF2YSAmIFN5c3RlbSBEZXNpZ24gTWFzdGVyY2xhc3NcIixcbiAgZGVzY3JpcHRpb246IFwiU2VuaW9yICYgU3RhZmYgTGV2ZWwgSW50ZXJ2aWV3IFByZXBhcmF0aW9uIEd1aWRlXCIsXG4gIFxuICB0aGVtZUNvbmZpZzoge1xuICAgIC8vIFx1RDgzRFx1REQwRCBFbmFibGUgSW5zdGFudCBMb2NhbCBGdWxsLVRleHQgU2VhcmNoXG4gICAgc2VhcmNoOiB7XG4gICAgICBwcm92aWRlcjogJ2xvY2FsJ1xuICAgIH0sXG5cbiAgICAvLyBcdUQ4M0VcdURERUQgVG9wIE5hdmlnYXRpb24gTGlua3NcbiAgICBuYXY6IFtcbiAgICAgIHsgdGV4dDogJ0hvbWUnLCBsaW5rOiAnLycgfSxcbiAgICAgIHsgdGV4dDogJ0xlYXJuaW5nIFBhdGgnLCBsaW5rOiAnL0xFQVJOSU5HX1BBVEgnIH0sXG4gICAgICB7IHRleHQ6ICdNYWluIFJlYWRNZScsIGxpbms6ICcvUkVBRE1FJyB9XG4gICAgXSxcblxuICAgIC8vIFx1RDgzRFx1RENEMSBDb21wcmVoZW5zaXZlIFNpZGViYXIgTWFwcGluZyBZb3VyIEZvbGRlcnNcbiAgICBzaWRlYmFyOiBbXG4gICAgICB7XG4gICAgICAgIHRleHQ6ICdHZXR0aW5nIFN0YXJ0ZWQnLFxuICAgICAgICBpdGVtczogW1xuICAgICAgICAgIHsgdGV4dDogJ1x1RDgzRFx1RENENiBNYXN0ZXIgUmVhZE1lJywgbGluazogJy9SRUFETUUnIH0sXG4gICAgICAgICAgeyB0ZXh0OiAnXHVEODNEXHVEREZBXHVGRTBGIExlYXJuaW5nIFBhdGgnLCBsaW5rOiAnL0xFQVJOSU5HX1BBVEgnIH0sXG4gICAgICAgICAgeyB0ZXh0OiAnXHVEODNEXHVEQ0NCIENhdGVnb3JpemVkIEZpbGVzJywgbGluazogJy9hbGxfbWFya2Rvd25fZmlsZXNfY2F0ZWdvcml6ZWQnIH1cbiAgICAgICAgXVxuICAgICAgfSxcbiAgICAgIHtcbiAgICAgICAgdGV4dDogJ0phdmEgQ29yZSAmIENvbmN1cnJlbmN5JyxcbiAgICAgICAgY29sbGFwc2VkOiBmYWxzZSxcbiAgICAgICAgaXRlbXM6IFtcbiAgICAgICAgICB7IHRleHQ6ICdcdTI2MTUgSmF2YSBDb3JlIEd1aWRlJywgbGluazogJy9qYXZhLWNvcmUvJyB9LCAvLyBQb2ludCB0byBmaWxlcyBpbnNpZGUgL2phdmEtY29yZSBpZiBtYXBwZWRcbiAgICAgICAgICB7IHRleHQ6ICdcdUQ4M0RcdURDREEgQ29sbGVjdGlvbnMgUmVmZXJlbmNlJywgbGluazogJy90b3BpY3MvamF2YV9jb2xsZWN0aW9uc19tYXN0ZXJ5JyB9XG4gICAgICAgIF1cbiAgICAgIH0sXG4gICAgICB7XG4gICAgICAgIHRleHQ6ICdGcmFtZXdvcmtzICYgQXJjaGl0ZWN0dXJlJyxcbiAgICAgICAgY29sbGFwc2VkOiBmYWxzZSxcbiAgICAgICAgaXRlbXM6IFtcbiAgICAgICAgICB7IHRleHQ6ICdcdUQ4M0NcdURGNDMgU3ByaW5nIE1hc3RlciBHdWlkZScsIGxpbms6ICcvc3ByaW5nLWZyYW1ld29yay8nIH0sXG4gICAgICAgICAgeyB0ZXh0OiAnXHVEODNEXHVEQ0U2IEphY2tzb24gU2VyaWFsaXphdGlvbicsIGxpbms6ICcvdG9waWNzL2phY2tzb25fbWFzdGVyX2d1aWRlJyB9XG4gICAgICAgIF1cbiAgICAgIH0sXG4gICAgICB7XG4gICAgICAgIHRleHQ6ICdTY2VuYXJpb3MgJiBQcmFjdGljZScsXG4gICAgICAgIGNvbGxhcHNlZDogZmFsc2UsXG4gICAgICAgIGl0ZW1zOiBbXG4gICAgICAgICAgeyB0ZXh0OiAnXHVEODNEXHVERDI1IE1hc3RlciBTY2VuYXJpb3MnLCBsaW5rOiAnL3NjZW5hcmlvcy8nIH1cbiAgICAgICAgXVxuICAgICAgfVxuICAgIF0sXG5cbiAgICBzb2NpYWxMaW5rczogW1xuICAgICAgeyBpY29uOiAnZ2l0aHViJywgbGluazogJ2h0dHBzOi8vZ2l0aHViLmNvbS8nIH1cbiAgICBdXG4gIH1cbn0pIl0sCiAgIm1hcHBpbmdzIjogIjtBQUE0VSxTQUFTLG9CQUFvQjtBQUV6VyxJQUFPLGlCQUFRLGFBQWE7QUFBQSxFQUMxQixPQUFPO0FBQUEsRUFDUCxhQUFhO0FBQUEsRUFFYixhQUFhO0FBQUE7QUFBQSxJQUVYLFFBQVE7QUFBQSxNQUNOLFVBQVU7QUFBQSxJQUNaO0FBQUE7QUFBQSxJQUdBLEtBQUs7QUFBQSxNQUNILEVBQUUsTUFBTSxRQUFRLE1BQU0sSUFBSTtBQUFBLE1BQzFCLEVBQUUsTUFBTSxpQkFBaUIsTUFBTSxpQkFBaUI7QUFBQSxNQUNoRCxFQUFFLE1BQU0sZUFBZSxNQUFNLFVBQVU7QUFBQSxJQUN6QztBQUFBO0FBQUEsSUFHQSxTQUFTO0FBQUEsTUFDUDtBQUFBLFFBQ0UsTUFBTTtBQUFBLFFBQ04sT0FBTztBQUFBLFVBQ0wsRUFBRSxNQUFNLDJCQUFvQixNQUFNLFVBQVU7QUFBQSxVQUM1QyxFQUFFLE1BQU0saUNBQXFCLE1BQU0saUJBQWlCO0FBQUEsVUFDcEQsRUFBRSxNQUFNLCtCQUF3QixNQUFNLGtDQUFrQztBQUFBLFFBQzFFO0FBQUEsTUFDRjtBQUFBLE1BQ0E7QUFBQSxRQUNFLE1BQU07QUFBQSxRQUNOLFdBQVc7QUFBQSxRQUNYLE9BQU87QUFBQSxVQUNMLEVBQUUsTUFBTSwwQkFBcUIsTUFBTSxjQUFjO0FBQUE7QUFBQSxVQUNqRCxFQUFFLE1BQU0sbUNBQTRCLE1BQU0sbUNBQW1DO0FBQUEsUUFDL0U7QUFBQSxNQUNGO0FBQUEsTUFDQTtBQUFBLFFBQ0UsTUFBTTtBQUFBLFFBQ04sV0FBVztBQUFBLFFBQ1gsT0FBTztBQUFBLFVBQ0wsRUFBRSxNQUFNLGlDQUEwQixNQUFNLHFCQUFxQjtBQUFBLFVBQzdELEVBQUUsTUFBTSxtQ0FBNEIsTUFBTSwrQkFBK0I7QUFBQSxRQUMzRTtBQUFBLE1BQ0Y7QUFBQSxNQUNBO0FBQUEsUUFDRSxNQUFNO0FBQUEsUUFDTixXQUFXO0FBQUEsUUFDWCxPQUFPO0FBQUEsVUFDTCxFQUFFLE1BQU0sOEJBQXVCLE1BQU0sY0FBYztBQUFBLFFBQ3JEO0FBQUEsTUFDRjtBQUFBLElBQ0Y7QUFBQSxJQUVBLGFBQWE7QUFBQSxNQUNYLEVBQUUsTUFBTSxVQUFVLE1BQU0sc0JBQXNCO0FBQUEsSUFDaEQ7QUFBQSxFQUNGO0FBQ0YsQ0FBQzsiLAogICJuYW1lcyI6IFtdCn0K
