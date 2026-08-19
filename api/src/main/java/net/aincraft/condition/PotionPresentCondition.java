package net.aincraft.condition;

import net.kyori.adventure.key.Key;

/**
 * True when the snapshot has the named potion effect.
 */
public record PotionPresentCondition(Key effectKey) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present()) {
      return false;
    }
    for (Key key : context.effects().keySet()) {
      if (BiomeCondition.keysEqual(effectKey, key)) {
        return true;
      }
    }
    return false;
  }
}
