package net.aincraft.condition;

/**
 * Matches {@link ConditionContext#sneaking()} against {@code expected}.
 */
public record SneakingCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present()) {
      return false;
    }
    return context.sneaking() == expected;
  }
}
