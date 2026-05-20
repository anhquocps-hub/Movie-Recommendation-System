"use client";

/**
 * CinemaHeroBackdrop
 * ==================
 * Pure-CSS cinematic background effects layered together:
 *
 *  1. Projector spotlight beam — diagonal cones of warm light
 *  2. Warm glow — ambient golden light from center/bottom
 *  3. Bokeh circles — soft out-of-focus warm orbs, floating
 *  4. Film grain — animated noise texture overlay
 *  5. Vignette — dark edges framing the scene
 *  6. Film strips — subtle decorative borders (left + right)
 *
 * All GPU-friendly (transform + opacity only), no JS on scroll.
 */
export function CinemaHeroBackdrop() {
  return (
    <div className="cinema-backdrop" aria-hidden="true">
      {/* Layer 1: Projector spotlight beam */}
      <div className="cinema-projector" />

      {/* Layer 2: Warm ambient glow */}
      <div className="cinema-glow" />

      {/* Layer 3: Bokeh circles */}
      <div className="cinema-bokeh">
        <span className="bokeh b1" />
        <span className="bokeh b2" />
        <span className="bokeh b3" />
        <span className="bokeh b4" />
        <span className="bokeh b5" />
        <span className="bokeh b6" />
        <span className="bokeh b7" />
      </div>

      {/* Layer 4: Film grain overlay */}
      <div className="cinema-grain" />

      {/* Layer 5: Vignette */}
      <div className="cinema-vignette" />

      {/* Layer 6: Film strip decorative edges */}
      <div className="cinema-filmstrip" />
      <div className="cinema-filmstrip-right" />
    </div>
  );
}
