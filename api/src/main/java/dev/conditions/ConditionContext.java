package dev.conditions;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

/**
 * Snapshot of player/world state used to evaluate {@link Condition}s. Paper
 * fills this from a live player; tests construct it directly.
 *
 * @param present {@code false} when the player is offline or unknown; most
 *     conditions fail closed
 */
public record ConditionContext(
    boolean present,
    boolean sneaking,
    boolean sprinting,
    @Nullable Key biome,
    @Nullable String worldName,
    @Nullable Key worldKey,
    @Nullable WeatherState weather,
    @Nullable Key fluid,
    @Nullable Double health,
    @Nullable Double hunger,
    @Nullable Double experience,
    Map<Key, PotionEffectSnapshot> effects,
    Set<String> jobKeys
) {

  public ConditionContext {
    effects = Map.copyOf(effects == null ? Map.of() : effects);
    jobKeys = Set.copyOf(jobKeys == null ? Set.of() : jobKeys);
  }

  public static ConditionContext absent() {
    return builder().present(false).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private boolean present = true;
    private boolean sneaking;
    private boolean sprinting;
    private @Nullable Key biome;
    private @Nullable String worldName;
    private @Nullable Key worldKey;
    private @Nullable WeatherState weather;
    private @Nullable Key fluid;
    private @Nullable Double health;
    private @Nullable Double hunger;
    private @Nullable Double experience;
    private Map<Key, PotionEffectSnapshot> effects = Map.of();
    private Set<String> jobKeys = Set.of();

    private Builder() {}

    public Builder present(boolean present) {
      this.present = present;
      return this;
    }

    public Builder sneaking(boolean sneaking) {
      this.sneaking = sneaking;
      return this;
    }

    public Builder sprinting(boolean sprinting) {
      this.sprinting = sprinting;
      return this;
    }

    public Builder biome(@Nullable Key biome) {
      this.biome = biome;
      return this;
    }

    public Builder worldName(@Nullable String worldName) {
      this.worldName = worldName;
      return this;
    }

    public Builder worldKey(@Nullable Key worldKey) {
      this.worldKey = worldKey;
      return this;
    }

    public Builder weather(@Nullable WeatherState weather) {
      this.weather = weather;
      return this;
    }

    public Builder fluid(@Nullable Key fluid) {
      this.fluid = fluid;
      return this;
    }

    public Builder health(@Nullable Double health) {
      this.health = health;
      return this;
    }

    public Builder hunger(@Nullable Double hunger) {
      this.hunger = hunger;
      return this;
    }

    public Builder experience(@Nullable Double experience) {
      this.experience = experience;
      return this;
    }

    public Builder effects(Map<Key, PotionEffectSnapshot> effects) {
      this.effects = Objects.requireNonNull(effects);
      return this;
    }

    public Builder jobKeys(Set<String> jobKeys) {
      this.jobKeys = Objects.requireNonNull(jobKeys);
      return this;
    }

    public ConditionContext build() {
      return new ConditionContext(
          present,
          sneaking,
          sprinting,
          biome,
          worldName,
          worldKey,
          weather,
          fluid,
          health,
          hunger,
          experience,
          effects,
          jobKeys);
    }
  }
}
