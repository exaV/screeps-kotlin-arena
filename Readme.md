# Screeps Kotlin Arena Starter

This project provides a starting point for playing [Screeps Arena](https://arena.screeps.com/) with Kotlin.

## Getting started:

```shell
./gradlew setup-screeps-arenas
```

Point your Arena client at [one of the arenas](arenas/tutorial) and hit play.

## Updating code

`gradle build` is sufficient

The setup-screeps-arenas will create links from the node_modules
folder in each arena subfolder to gradle's build directory which keeps changes in sync.

## Adding a new Arena

Copy one of the existing subfolder of [arenas](arenas) 
and change the main.mjs file so it imports the correct entrypoint.

Afterward you must run `gradle setup-screeps-arenas` once.

## Types

[Type definitions](types) are derived from the TypeScript definitions provided by the Screeps Arena client and should be the same
as the [official documentation](https://arena.screeps.com/docs). If you spot any errors please create a pull request.

The original d.ts files are included in the repo so we can easily see what changes between versions 
and update the Kotlin definitions accordingly.

## FAQ

- Error "module not found": run `gradle setup-screeps-arenas` first