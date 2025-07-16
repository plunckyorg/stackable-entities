package com.pluncky.stackableentities.util;

import org.bukkit.entity.*;

public class CreatureUtils {
    public static void normalizeCreature(Creature creature) {
        if (creature == null) return;

        if (creature instanceof Ageable ageable) {
           ageable.setAdult();
        }

        if (creature instanceof Slime slime) {
            slime.setSize(1);
        }
    }
}
