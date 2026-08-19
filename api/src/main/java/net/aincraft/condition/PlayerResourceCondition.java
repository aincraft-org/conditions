package net.aincraft.condition;

import java.math.BigDecimal;

/**
 * Compares a player resource snapshot value to {@code expected}.
 */
public record PlayerResourceCondition(
    PlayerResourceType type, RelationalOperator operator, double expected) implements Condition {

  @Override
  public boolean test(ConditionContext context) {
    if (!context.present()) {
      return false;
    }
    Double actual = switch (type) {
      case HEALTH -> context.health();
      case HUNGER -> context.hunger();
      case EXPERIENCE -> context.experience();
    };
    if (actual == null) {
      return false;
    }
    return operator.test(BigDecimal.valueOf(actual), BigDecimal.valueOf(expected));
  }
}
