package net.aincraft.condition.paper;

import net.aincraft.condition.ConditionContext;
import net.aincraft.condition.PotionEffectSnapshot;
import net.aincraft.condition.WeatherState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.key.Key;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

/**
 * Builds a {@link ConditionContext} from a live Paper player.
 */
public final class PaperConditionContexts {

  private PaperConditionContexts() {}

  /** Snapshot of {@code player} with no job keys. */
  public static ConditionContext from(@Nullable Player player) {
    return from(player, Set.of());
  }

  /**
   * Snapshot of {@code player} plus optional job keys (namespaced or bare).
   */
  public static ConditionContext from(
      @Nullable Player player, @Nullable Set<String> jobKeys) {
    if (player == null || !player.isOnline()) {
      return ConditionContext.absent();
    }
    World world = player.getWorld();
    WeatherState weather = world.isThundering()
        ? WeatherState.THUNDERING
        : world.hasStorm() ? WeatherState.RAINING : WeatherState.CLEAR;
    Key fluid = player.isInWater()
        ? Key.key("minecraft:water")
        : player.isInLava() ? Key.key("minecraft:lava") : null;
    Map<Key, PotionEffectSnapshot> effects = new HashMap<>();
    for (PotionEffect effect : player.getActivePotionEffects()) {
      effects.put(
          effect.getType().getKey(),
          new PotionEffectSnapshot(effect.getAmplifier(), effect.getDuration()));
    }
    return ConditionContext.builder()
        .present(true)
        .sneaking(player.isSneaking())
        .sprinting(player.isSprinting())
        .biome(world.getBiome(player.getLocation()).getKey())
        .worldName(world.getName())
        .worldKey(world.getKey())
        .weather(weather)
        .fluid(fluid)
        .health(player.getHealth())
        .hunger((double) player.getFoodLevel())
        .experience((double) player.getExp())
        .effects(effects)
        .jobKeys(jobKeys == null ? Set.of() : jobKeys)
        .build();
  }
}
