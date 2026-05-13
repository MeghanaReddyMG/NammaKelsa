---
name: Blue-Collar Professionalism
colors:
  surface: '#faf9fc'
  surface-dim: '#dad9dd'
  surface-bright: '#faf9fc'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f4f3f7'
  surface-container: '#eeedf1'
  surface-container-high: '#e9e7eb'
  surface-container-highest: '#e3e2e6'
  on-surface: '#1a1c1e'
  on-surface-variant: '#43474e'
  inverse-surface: '#2f3033'
  inverse-on-surface: '#f1f0f4'
  outline: '#74777f'
  outline-variant: '#c4c6cf'
  surface-tint: '#455f87'
  primary: '#022448'
  on-primary: '#ffffff'
  primary-container: '#1e3a5f'
  on-primary-container: '#8aa4cf'
  inverse-primary: '#adc8f5'
  secondary: '#0061a5'
  on-secondary: '#ffffff'
  secondary-container: '#6fb2fd'
  on-secondary-container: '#004375'
  tertiary: '#341f00'
  on-tertiary: '#ffffff'
  tertiary-container: '#503300'
  on-tertiary-container: '#c69b5f'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d5e3ff'
  primary-fixed-dim: '#adc8f5'
  on-primary-fixed: '#001c3b'
  on-primary-fixed-variant: '#2d486d'
  secondary-fixed: '#d2e4ff'
  secondary-fixed-dim: '#a0caff'
  on-secondary-fixed: '#001c37'
  on-secondary-fixed-variant: '#00497e'
  tertiary-fixed: '#ffddb2'
  tertiary-fixed-dim: '#edbf7f'
  on-tertiary-fixed: '#291800'
  on-tertiary-fixed-variant: '#60410c'
  background: '#faf9fc'
  on-background: '#1a1c1e'
  surface-variant: '#e3e2e6'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  margin-mobile: 16px
  gutter-mobile: 12px
---

## Brand & Style
The design system is engineered to bridge the gap between labor and enterprise through a visual language of reliability and speed. The target audience includes both daily wage workers looking for immediate opportunities and employers seeking vetted, professional help. 

The style is **Corporate / Modern** with a strong influence from Material Design 3. It prioritizes functional clarity over decorative flair, using generous whitespace to reduce cognitive load during the booking process. The aesthetic evokes a sense of "digital utility"—it feels like a tool that works as hard as its users. High-contrast elements ensure readability in outdoor environments, while the structured hierarchy communicates efficiency and institutional trust.

## Colors
The color palette is anchored by a deep, authoritative Dark Blue to establish professional trust. The Accent Orange is reserved strictly for high-priority Call-to-Actions (CTAs) and "Book Now" triggers to ensure they are immediately discoverable.

Functional status colors (Success, Warning, Error) follow industry standards but are applied with softened background tints to maintain the clean aesthetic. Surfaces utilize a crisp white, while the background uses a cool-toned light grey to define depth and separate content blocks without the need for heavy borders.

## Typography
This design system utilizes **Inter** for its exceptional legibility on mobile screens and its neutral, systematic character. The typographic scale is exaggerated to favor accessibility; headings are bold and large to provide clear "landmarks" for users who may be navigating the app quickly.

Line heights are set to 1.5x for body text to ensure readability for users with varying literacy levels. Tight letter-spacing is applied to larger headlines to maintain a modern, compact feel, while labels remain clear for navigation and metadata.

## Layout & Spacing
The layout follows a **Fluid Grid** model optimized for mobile-first interactions. It uses an 8px baseline grid to ensure all components align with a predictable rhythm. 

Standard mobile screens use 16px horizontal margins to maximize real estate while maintaining a safe "thumb zone." Padding within cards and containers is typically set to 16px (md) to provide internal breathing room. Vertical rhythm is maintained by using 24px (lg) spacing between distinct content sections.

## Elevation & Depth
Depth is communicated through **Tonal Layers** and **Ambient Shadows**. The background layer resides at 0dp, while Surface containers (cards) sit at 1dp or 2dp elevation. 

Shadows are diffused and low-opacity, using a subtle tint of the Primary color (#1E3A5F) rather than pure black to keep the UI looking clean and integrated. This "soft-depth" approach guides the user's eye to interactive elements like worker profiles and booking cards without overwhelming the flat, modern aesthetic. High-elevation shadows (4dp+) are reserved exclusively for floating action buttons or temporary overlays like bottom sheets.

## Shapes
The shape language balances friendliness with structural integrity. A standard **12px to 16px radius** is applied to cards and containers to soften the professional tone, making the platform feel approachable. 

Buttons utilize a 12px radius, aligning with the primary card structures. Smaller UI elements like input fields use an 8px radius. Secondary elements like status indicators and category chips use a fully circular (pill) radius to distinguish them from structural, rectangular components.

## Components

### Buttons
- **Primary:** Solid #1E3A5F background with White text. 12px corner radius. High-emphasis for final actions.
- **Secondary (CTA):** Solid #FF6B35 background with White text. Used for "Book Now" or "Apply" to draw immediate attention.
- **Tertiary:** Outlined with #1E3A5F and matching text. Used for secondary actions like "View Details."

### Cards
Cards are the core vessel for worker profiles and job postings. They feature a white surface, a 16px border-radius, and a soft 1dp shadow. Internal padding is a consistent 16px.

### Chips & Tags
- **Category Chips:** Pill-shaped, light grey background with Dark Blue text.
- **Status Badges:** Pill-shaped with soft-tints of success/warning/error colors. Text is a high-contrast version of the background tint (e.g., Light Green background with Dark Green text).

### Inputs & Controls
Input fields are outlined with a light grey border, 8px radius, and use the Primary Light color for the active focus state.

### Bottom Sheets
Used extensively for filters and worker selection details to keep the user within the context of the main map or list view. They feature 24px top-corner rounding and a clear "handle" indicator.