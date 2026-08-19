package net.aincraft.condition;

/**
 * Condition that always holds. Used as a base rule with no restrictions.
 */
public record AlwaysCondition() implements Condition {

  public static final AlwaysCondition INSTANCE = new AlwaysCondition();

  @Override
  public boolean test(ConditionContext context) {
    return true;
  }
}
