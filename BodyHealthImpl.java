package com.bodyhealthmod.common.capability;

import com.bodyhealthmod.common.damage.BodyPart;
import com.bodyhealthmod.common.damage.BodyPartHealth;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class BodyHealthImpl implements IBodyHealth {

    private static final Random RNG = new Random();

    private final Map<BodyPart, BodyPartHealth> parts = new EnumMap<>(BodyPart.class);

    public BodyHealthImpl() {
        for (BodyPart bp : BodyPart.values()) {
            parts.put(bp, new BodyPartHealth(bp));
        }
    }

    @Override
    public Map<BodyPart, BodyPartHealth> getParts() {
        return parts;
    }

    @Override
    public BodyPartHealth getPart(BodyPart part) {
        return parts.get(part);
    }

    /**
     * Picks a weighted-random body part and records the vanilla damage value on it.
     * This does NOT change the damage, does NOT affect player HP — display only.
     */
    @Override
    public void recordHit(float damage) {
        BodyPart hit = BodyPart.randomWeighted(RNG);
        parts.get(hit).recordHit(damage);
    }

    @Override
    public void resetAll() {
        for (BodyPartHealth bph : parts.values()) bph.reset();
    }
}
