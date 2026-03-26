package com.bodyhealthmod.common.damage;

/**
 * Represents each body part tracked by the mod.
 * Used purely for display — which part was hit and how much damage was taken.
 * No health pools, no effects, no influence on vanilla game mechanics.
 */
public enum BodyPart {

    HEAD      ("Head"),
    CHEST     ("Chest"),
    LEFT_ARM  ("Left Arm"),
    RIGHT_ARM ("Right Arm"),
    LEFT_LEG  ("Left Leg"),
    RIGHT_LEG ("Right Leg");

    public final String displayName;

    BodyPart(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Weighted random selection of which part was hit.
     * Larger body parts have a higher chance of being selected.
     * These weights are cosmetic only — they do not affect damage.
     */
    public static BodyPart randomWeighted(java.util.Random rng) {
        // Weights: chest biggest, arms smallest
        int[] weights = { 10, 20, 8, 8, 14, 14 }; // HEAD, CHEST, L_ARM, R_ARM, L_LEG, R_LEG
        int total = 0;
        for (int w : weights) total += w;
        int roll = rng.nextInt(total);
        int cumulative = 0;
        BodyPart[] parts = values();
        for (int i = 0; i < parts.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) return parts[i];
        }
        return CHEST;
    }
}
