package dev.conditions.store;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import net.kyori.adventure.key.Key;

/**
 * Light namespaced primitive bag, PDC-shaped but Paper-free. The whole bag
 * encodes to a Kryo {@code byte[]} (boolean, int, long, float, double, string,
 * byte[]). Missing or wrong-typed keys are empty, never thrown.
 */
public final class DataBag {

  private static final byte BOOL = 1;
  private static final byte INT = 2;
  private static final byte LONG = 3;
  private static final byte FLOAT = 4;
  private static final byte DOUBLE = 5;
  private static final byte STRING = 6;
  private static final byte BYTES = 7;

  private final Map<String, Entry> values = new LinkedHashMap<>();

  private DataBag() {}

  public static DataBag create() {
    return new DataBag();
  }

  public boolean has(Key key) {
    return values.containsKey(id(key));
  }

  public DataBag setBoolean(Key key, boolean value) {
    values.put(id(key), new Entry(BOOL, value));
    return this;
  }

  public DataBag setInt(Key key, int value) {
    values.put(id(key), new Entry(INT, value));
    return this;
  }

  public DataBag setLong(Key key, long value) {
    values.put(id(key), new Entry(LONG, value));
    return this;
  }

  public DataBag setFloat(Key key, float value) {
    values.put(id(key), new Entry(FLOAT, value));
    return this;
  }

  public DataBag setDouble(Key key, double value) {
    values.put(id(key), new Entry(DOUBLE, value));
    return this;
  }

  public DataBag setString(Key key, String value) {
    values.put(id(key), new Entry(STRING, Objects.requireNonNull(value)));
    return this;
  }

  public DataBag setBytes(Key key, byte[] value) {
    values.put(id(key), new Entry(BYTES, value.clone()));
    return this;
  }

  public Optional<Boolean> getBoolean(Key key) {
    return typed(key, BOOL, Boolean.class);
  }

  public OptionalInt getInt(Key key) {
    return typed(key, INT, Integer.class).map(OptionalInt::of).orElseGet(OptionalInt::empty);
  }

  public OptionalLong getLong(Key key) {
    return typed(key, LONG, Long.class).map(OptionalLong::of).orElseGet(OptionalLong::empty);
  }

  public Optional<Float> getFloat(Key key) {
    return typed(key, FLOAT, Float.class);
  }

  public OptionalDouble getDouble(Key key) {
    return typed(key, DOUBLE, Double.class).map(OptionalDouble::of).orElseGet(OptionalDouble::empty);
  }

  public Optional<String> getString(Key key) {
    return typed(key, STRING, String.class);
  }

  public Optional<byte[]> getBytes(Key key) {
    return typed(key, BYTES, byte[].class).map(byte[]::clone);
  }

  /**
   * Kryo-framed byte array of this bag. This is the payload written to a Paper
   * PDC as {@code PersistentDataType.BYTE_ARRAY}.
   */
  public byte[] toBytes() {
    Output output = new Output(256, -1);
    output.writeVarInt(values.size(), true);
    for (Map.Entry<String, Entry> e : values.entrySet()) {
      output.writeString(e.getKey());
      Entry entry = e.getValue();
      output.writeByte(entry.tag);
      switch (entry.tag) {
        case BOOL -> output.writeBoolean((Boolean) entry.value);
        case INT -> output.writeInt((Integer) entry.value, false);
        case LONG -> output.writeLong((Long) entry.value, false);
        case FLOAT -> output.writeFloat((Float) entry.value);
        case DOUBLE -> output.writeDouble((Double) entry.value);
        case STRING -> output.writeString((String) entry.value);
        case BYTES -> {
          byte[] blob = (byte[]) entry.value;
          output.writeVarInt(blob.length, true);
          output.writeBytes(blob);
        }
        default -> throw new IllegalStateException("unknown tag " + entry.tag);
      }
    }
    return output.toBytes();
  }

  public static DataBag fromBytes(byte[] bytes) {
    DataBag bag = create();
    if (bytes == null || bytes.length == 0) {
      return bag;
    }
    Input input = new Input(bytes);
    int count = input.readVarInt(true);
    for (int i = 0; i < count; i++) {
      String key = input.readString();
      byte tag = input.readByte();
      Object value = switch (tag) {
        case BOOL -> input.readBoolean();
        case INT -> input.readInt(false);
        case LONG -> input.readLong(false);
        case FLOAT -> input.readFloat();
        case DOUBLE -> input.readDouble();
        case STRING -> input.readString();
        case BYTES -> {
          int length = input.readVarInt(true);
          yield input.readBytes(length);
        }
        default -> throw new IllegalArgumentException("unknown tag " + tag);
      };
      bag.values.put(key, new Entry(tag, value));
    }
    return bag;
  }

  private <T> Optional<T> typed(Key key, byte tag, Class<T> type) {
    Entry entry = values.get(id(key));
    if (entry == null || entry.tag != tag) {
      return Optional.empty();
    }
    return Optional.of(type.cast(entry.value));
  }

  private static String id(Key key) {
    return Objects.requireNonNull(key, "key").asString();
  }

  private record Entry(byte tag, Object value) {}
}
