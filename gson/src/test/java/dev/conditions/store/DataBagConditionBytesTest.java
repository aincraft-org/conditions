package dev.conditions.store;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.conditions.Condition;
import dev.conditions.ConditionContext;
import dev.conditions.ConditionSerializer;
import dev.conditions.Conditions;
import dev.conditions.gson.GsonConditionSerializer;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

class DataBagConditionBytesTest {

  private static final Key CONDITION = Key.key("modularjobs", "condition");

  @Test
  void conditionSerializerBytesSurviveBagRoundTrip() {
    ConditionSerializer json = GsonConditionSerializer.gson();
    Condition original = Conditions.allOf(
        Conditions.sneaking(true),
        Conditions.world("world_nether"));
    byte[] conditionBytes = json.write(original);

    DataBag bag = DataBag.create().setBytes(CONDITION, conditionBytes);
    DataBag restored = DataBag.fromBytes(bag.toBytes());
    byte[] recovered = restored.getBytes(CONDITION).orElseThrow();

    Condition rehydrated = json.read(recovered);
    ConditionContext matching = ConditionContext.builder()
        .present(true)
        .sneaking(true)
        .worldName("world_nether")
        .build();
    ConditionContext standing = ConditionContext.builder()
        .present(true)
        .sneaking(false)
        .worldName("world_nether")
        .build();
    assertTrue(rehydrated.test(matching));
    assertFalse(rehydrated.test(standing));
    assertFalse(rehydrated.test(ConditionContext.absent()));
  }
}
