# Screeps Kotlin Arena Starter

This project provides a starting point for playing [Screeps Arena](https://arena.screeps.com/) with Kotlin.

## Getting started:

```shell
./gradlew build-screeps-arena
```

Point your Arena client at [starter/jsconfig.json](starter/jsconfig.json) and hit play.

## Types

[Type definitions](types) are derived from the TypeScript definitions provided by the Screeps Arena client and should be the same
as the [official documentation](https://arena.screeps.com/docs). If you spot any errors please create a pull request.

The original d.ts files are included in the repo so we can easily see what changes between versions 
and update the Kotlin definitions accordingly.

## FAQ

- Error "module not found": run `gradle build-screeps-arena` first