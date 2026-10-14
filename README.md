# Tags

Chat tags for Paper 1.21.11. Players redeem tag notes and equip them through `/tags`.
Includes hex colours, gradients and an API for other plugins.

Chat layout is set in `config.yml`, including where LuckPerms prefixes appear.
Tag names, colours and note items are in `tag.yml`.

Requires Java 21 or newer.

## Folder structure

```text
src/main/java/me/alex/tags/
  Tags.java          Plugin entry point
  api/               TagsAPI
  chat/              Chat formatting and LuckPerms prefixes
  command/           /tags
  config/            Config loading
  data/              Player saves
  gui/               Tag menu
  item/              Tag notes
  listener/          Event handlers
  model/             Tags and player data
  text/              Colour formatting
src/main/resources/
  config.yml         Chat settings
  plugin.yml         Plugin metadata
  tag.yml            Tags and notes
pom.xml              Maven build
```

Player data is saved in `plugins/Tags/players/<uuid>.yml`.

## Building

Run this from the project folder:

```sh
mvn clean package -Dmaven.test.skip=true
```

The JAR is `target/Tags-1.0.0.jar`. Put it in your server's `plugins` folder and restart.
