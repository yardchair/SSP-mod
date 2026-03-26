package com.bodyhealthmod.common.capability;

import com.bodyhealthmod.common.damage.BodyPart;
import com.bodyhealthmod.common.damage.BodyPartHealth;

import java.util.Map;

/**
 * Capability interface - purely cosmetic.
 * Records which body part was hit and how much damage, for HUD display only.
 * Has zero influence on vanilla health, damage, or any game mechanic.
 */
public interface IBodyHealth {

    /** All body part display containers, keyed by part. */
    Map<BodyPart, BodyPartHealth> getParts();

    BodyPartHealth getPart(BodyPart part);

    /**
     * Record a hit on a randomly selected body part.
     * The damage value is the actual vanilla damage amount - displayed as-is.
     * Nothing is modified; this is cosmetic only.
     */
    void recordHit(float damage);

    /** Clear all hit displays (e.g. on respawn). */
    void resetAll();
}
