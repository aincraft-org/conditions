package dev.conditions;

/**
 * Pure player predicate. Implementations must not look up Bukkit state; they
 * read only {@link ConditionContext}.
 */
@FunctionalInterface
public interface Condition {

  /**
   * Returns {@code true} when this predicate holds for {@code context}.
   */
  boolean test(ConditionContext context);

  default Condition and(Condition other) {
    return Conditions.allOf(this, other);
  }

  default Condition or(Condition other) {
    return Conditions.anyOf(this, other);
  }

  default Condition negate() {
    return Conditions.inverted(this);
  }
}
