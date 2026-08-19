package net.aincraft.condition.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import net.aincraft.condition.Condition;
import net.aincraft.condition.ConditionContext;
import net.aincraft.condition.ConditionSerializer;
import net.aincraft.condition.Conditions;
import net.aincraft.condition.SneakingCondition;
import org.junit.jupiter.api.Test;

class GsonConditionSerializerTest {

  private final ConditionSerializer serializer = GsonConditionSerializer.gson();

  @Test
  void writesSneakingAsVanillaEntityProperties() {
    byte[] bytes = serializer.write(Conditions.sneaking(true));
    JsonObject json = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8))
        .getAsJsonObject();
    assertEquals("minecraft:entity_properties", json.get("condition").getAsString());
    assertEquals("this", json.get("entity").getAsString());
    assertTrue(json.getAsJsonObject("predicate")
        .getAsJsonObject("flags")
        .get("is_sneaking")
        .getAsBoolean());
  }

  @Test
  void readsVanillaSneakingJson() {
    String json = """
        {
          "condition": "minecraft:entity_properties",
          "entity": "this",
          "predicate": { "flags": { "is_sneaking": true } }
        }
        """;
    Condition condition = serializer.read(json.getBytes(StandardCharsets.UTF_8));
    assertInstanceOf(SneakingCondition.class, condition);
    assertTrue(condition.test(
        ConditionContext.builder().present(true).sneaking(true).build()));
  }

  @Test
  void unknownConditionIdThrowsOnRead() {
    String json = """
        { "condition": "minecraft:random_chance", "chance": 0.5 }
        """;
    assertThrows(IllegalArgumentException.class,
        () -> serializer.read(json.getBytes(StandardCharsets.UTF_8)));
  }
}
