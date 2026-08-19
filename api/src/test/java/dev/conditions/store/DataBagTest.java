package dev.conditions.store;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

class DataBagTest {

  private static final Key FLAG = Key.key("modularjobs", "flag");
  private static final Key COUNT = Key.key("modularjobs", "count");
  private static final Key TICKS = Key.key("modularjobs", "ticks");
  private static final Key RATIO = Key.key("modularjobs", "ratio");
  private static final Key AMOUNT = Key.key("modularjobs", "amount");
  private static final Key NAME = Key.key("modularjobs", "name");
  private static final Key BLOB = Key.key("modularjobs", "blob");
  private static final Key MISSING = Key.key("modularjobs", "missing");

  @Test
  void roundTripsMixedPrimitivesThroughKryoBytes() {
    DataBag bag = DataBag.create()
        .setBoolean(FLAG, true)
        .setInt(COUNT, 42)
        .setLong(TICKS, 9_000_000_000L)
        .setFloat(RATIO, 1.5f)
        .setDouble(AMOUNT, 3.25d)
        .setString(NAME, "mining_helmet")
        .setBytes(BLOB, new byte[] {1, 2, 3, 4});

    byte[] encoded = bag.toBytes();
    assertTrue(encoded.length > 0);

    DataBag restored = DataBag.fromBytes(encoded);
    assertEquals(Optional.of(true), restored.getBoolean(FLAG));
    assertEquals(OptionalInt.of(42), restored.getInt(COUNT));
    assertEquals(OptionalLong.of(9_000_000_000L), restored.getLong(TICKS));
    assertEquals(1.5f, restored.getFloat(RATIO).orElseThrow(), 0.0001f);
    assertEquals(OptionalDouble.of(3.25d), restored.getDouble(AMOUNT));
    assertEquals(Optional.of("mining_helmet"), restored.getString(NAME));
    assertArrayEquals(new byte[] {1, 2, 3, 4}, restored.getBytes(BLOB).orElseThrow());
  }

  @Test
  void missingKeyIsEmptyNotThrown() {
    DataBag bag = DataBag.create().setInt(COUNT, 1);
    DataBag restored = DataBag.fromBytes(bag.toBytes());
    assertFalse(restored.has(MISSING));
    assertTrue(restored.getBoolean(MISSING).isEmpty());
    assertTrue(restored.getInt(MISSING).isEmpty());
    assertTrue(restored.getLong(MISSING).isEmpty());
    assertTrue(restored.getFloat(MISSING).isEmpty());
    assertTrue(restored.getDouble(MISSING).isEmpty());
    assertTrue(restored.getString(MISSING).isEmpty());
    assertTrue(restored.getBytes(MISSING).isEmpty());
  }

  @Test
  void wrongTypeIsEmptyNotThrown() {
    DataBag bag = DataBag.fromBytes(DataBag.create().setString(NAME, "x").toBytes());
    assertTrue(bag.getInt(NAME).isEmpty());
    assertTrue(bag.getBytes(NAME).isEmpty());
  }
}
