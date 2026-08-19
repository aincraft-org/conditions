# conditions

Paper-free player predicates with a vanilla loot-condition JSON reader/writer.

Coordinates (`org.aincraft`):

| Artifact | Role |
|----------|------|
| `conditions-api` | Immutable `Condition` graph + `ConditionContext` |
| `conditions-gson` | `ConditionSerializer` — UTF-8 vanilla-shaped JSON bytes |
| `conditions-paper` | `PaperConditionContexts.from(Player, …)` snapshot adapter |

## Versioning

[CalVer](https://calver.org/) `YY.M.D.REVISION` (same contract as ModularJobs):

```text
26.8.19.1
```

- `YY` two-digit year, `M`/`D` unpadded month/day
- `REVISION` starts at `1` each calendar date
- Git tags: `v26.8.19.1`
- Local builds: `0.0.0-SNAPSHOT`
- Releases: `./gradlew publish -PreleaseVersion=YY.M.D.REVISION`

CI publishes to GitHub Packages using UTC `YY.M.D.<run-number>`.

## Build

```bash
./gradlew test
./gradlew publish          # sibling maven-repo at build/maven-repo
```

Java 21. `api` and `gson` have no Bukkit types.
