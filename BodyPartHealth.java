package com.bodyhealthmod.common.damage;

/**
 * Purely cosmetic data container for one body part.
 * Tracks only the last hit damage for display purposes.
 * No health pools, no effects, no influence on vanilla damage whatsoever.
 */
public class BodyPartHealth {

    private final BodyPart part;

    // The damage amount from the last hit — shown as a counter in the HUD.
    private float lastHitDamage  = 0f;
    private int   hitDisplayTicks = 0;

    private static final int HIT_DISPLAY_DURATION = 60; // 3 seconds at 20 tps

    public BodyPartHealth(BodyPart part) {
        this.part = part;
    }

    /**
     * Record a hit on this part for display purposes only.
     * Does NOT affect any health value.
     */
    public void recordHit(float damage) {
        this.lastHitDamage   = damage;
        this.hitDisplayTicks = HIT_DISPLAY_DURATION;
    }

    /** Clear the hit display (e.g. on respawn). */
    public void reset() {
        lastHitDamage   = 0f;
        hitDisplayTicks = 0;
    }

    /** Called each client tick to count down the damage counter display. */
    public void clientTick() {
        if (hitDisplayTicks > 0) hitDisplayTicks--;
    }

    public BodyPart getPart()            { return part; }
    public float    getLastHitDamage()   { return lastHitDamage; }
    public int      getHitDisplayTicks() { return hitDisplayTicks; }
}
