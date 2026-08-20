# conditions

Player predicates for Paper plugins. Java package and Maven group: **`dev.conditions`**.

| Artifact | Role |
|----------|------|
| `dev.conditions:api` | Immutable `Condition` graph + `ConditionContext` |
| `dev.conditions:gson` | Vanilla loot-condition JSON `read` / `write` (UTF-8 bytes) |
| `dev.conditions:paper` | `PaperConditionContexts.from(Player, jobKeys)` |

## Java

```java
import dev.conditions.Condition;
import dev.conditions.ConditionContext;
import dev.conditions.Conditions;

Condition netherSneak = Conditions.allOf(
    Conditions.world("world_nether"),
    Conditions.sneaking(true));

boolean matches = netherSneak.test(
    ConditionContext.builder()
        .present(true)
        .worldName("world_nether")
        .sneaking(true)
        .build());
```

Living entity (not a player) and block:

```java
Condition burningZombie = Conditions.allOf(
    Conditions.entityType(Key.key("minecraft:zombie")),
    Conditions.onFire(true));

Condition northChest = Conditions.allOf(
    Conditions.blockId(Key.key("minecraft:chest")),
    Conditions.blockProperty("facing", "north"));

// Player-only — a generic living snapshot fails closed
Condition survival = Conditions.gameMode("survival");
```

```json
{
  "condition": "minecraft:entity_properties",
  "entity": "this",
  "predicate": {
    "type": "minecraft:zombie",
    "flags": { "is_on_fire": true }
  }
}
```

```json
{
  "condition": "minecraft:block_state_property",
  "block": "minecraft:chest",
  "properties": { "facing": "north" }
}
```

On Paper, build the snapshot from a live player:

```java
import dev.conditions.paper.PaperConditionContexts;
import java.util.Set;

var ctx = PaperConditionContexts.from(player, Set.of("modularjobs:miner"));
boolean ok = netherSneak.test(ctx);

// Non-player living entity or a block:
PaperConditionContexts.fromLiving(zombie);
PaperConditionContexts.fromBlock(block);
```

## JSON (vanilla loot-condition shape)

Reader/writer:

```java
import dev.conditions.gson.GsonConditionSerializer;

var json = GsonConditionSerializer.gson();
byte[] bytes = json.write(netherSneak);
Condition back = json.read(bytes);
```

Sneaking:

```json
{
  "condition": "minecraft:entity_properties",
  "entity": "this",
  "predicate": { "flags": { "is_sneaking": true } }
}
```

Nether + sneaking:

```json
{
  "condition": "minecraft:all_of",
  "terms": [
    { "condition": "modularjobs:world", "world": "world_nether" },
    {
      "condition": "minecraft:entity_properties",
      "entity": "this",
      "predicate": { "flags": { "is_sneaking": true } }
    }
  ]
}
```

Weather:

```json
{ "condition": "minecraft:weather_check", "raining": true }
```

Low health:

```json
{
  "condition": "modularjobs:player_resource",
  "resource": "health",
  "operator": "<=",
  "value": 6.0
}
```

Job:

```json
{ "condition": "modularjobs:job", "jobs": ["miner"] }
```

Inverted:

```json
{
  "condition": "minecraft:inverted",
  "term": {
    "condition": "minecraft:entity_properties",
    "entity": "this",
    "predicate": { "flags": { "is_sprinting": true } }
  }
}
```

Boosts persist a rule as **priority + those JSON bytes + boost**:

```json
{
  "priority": 100,
  "conditions": "<base64 of the vanilla JSON above>",
  "boost": { "type": "multiplicative", "amount": 3.0 }
}
```

## Versioning

CalVer `YY.M.D.REVISION` (example `26.8.19.1`). Local: `0.0.0-SNAPSHOT`.
Release: `./gradlew publishAllPublicationsToLocalBuildRepoRepository -PreleaseVersion=26.8.19.1`.

## Primitive bag (PDC-shaped, Kryo `byte[]`)

Item embeds use [`mintychochip/databag`](https://github.com/mintychochip/databag)
(`dev.databag:databag`). Condition graphs stay vanilla JSON bytes in a `byte[]`
slot — they are not Kryo condition classes.

```java
import dev.databag.DataBag;
import net.kyori.adventure.key.Key;

DataBag bag = DataBag.create()
    .setBoolean(Key.key("modularjobs", "enabled"), true)
    .setInt(Key.key("modularjobs", "priority"), 100)
    .setBytes(Key.key("modularjobs", "condition"), json.write(condition));

byte[] pdcPayload = bag.toBytes();
DataBag back = DataBag.fromBytes(pdcPayload);
```

On an item: `PersistentBags.write(stack, namespacedKey, bag)`.

## Extension SPI

Vanilla / `modularjobs:*` kinds stay built-in. Anything ModularJobs should not
own (party, region, …) registers a `ConditionHandler`. JSON fields become a
`DataBag` (`condition:<field>`); evaluation extras live on
`ConditionContext.extras()`.

```java
ConditionHandlers.register(new ConditionHandler() {
  public Key id() { return Key.key("acme", "party_size"); }
  public Condition read(DataBag arguments) {
    int min = arguments.getInt(Key.key("condition", "min")).orElseThrow();
    return ctx -> ctx.extras().getInt(Key.key("acme", "party_size")).orElse(0) >= min;
  }
  public Optional<DataBag> write(Condition condition) { /* … */ }
});
```

```json
{ "condition": "acme:party_size", "min": 3 }
```

Paper fills extras without the conditions library knowing the plugin:

```java
DataBag extras = DataBag.create().setInt(Key.key("acme", "party_size"), partySize);
PaperConditionContexts.from(player, jobKeys, extras);
```

Unregistered ids still throw on read.

## Build

```bash
./gradlew test
./gradlew publishAllPublicationsToLocalBuildRepoRepository
```
