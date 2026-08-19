package net.aincraft.condition;

/**
 * Matches {@link ConditionContext#sprinting()} against {@code expected}.
 */
public record SprintingCondition(boolean expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present()) {
      return false;
    }
    return context.sprinting() == expected;
  }
}
