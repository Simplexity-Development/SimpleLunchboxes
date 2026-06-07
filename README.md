<div align="center">
  <table>
    <tr>
      <td valign="middle">
        <img
          width="120"
          height="120"
          alt=""
          src="https://github.com/user-attachments/assets/45f1ee48-eb43-41d7-afa0-62a75b7bb7cd"
        />
      </td>
      <td valign="middle">
        <h2>SimpleLunchboxes</h2>
        Portable food and potion storage for your adventures.<br>
        Right-click to eat, sneak + right-click to manage your inventory.
        <div style="line-height:20px;">&nbsp;</div>
      </td>
    </tr>
  </table>
</div>

## Items

### Lunchbox

A personal food storage item. Right-clicking eats the next food item stored inside. Sneak + right-click to open the inventory and manage its contents.

- **Live food tooltip:** the item matches what will be consumed next, AppleSkin users can see the hunger and saturation.
- **Variable item models:** the displayed model switches between `empty`, `low` (< 50% full), and `high` (>= 50% full) based on food fill level.
- **Tiers**: There are tiers 1-6 which allow storing more food per lunchbox.

### Gluttonous Lunchbox

Identical to the Lunchbox, but allows eating even when the player's hunger bar is full.

### Ender Lunchbox

Draws food directly from the player's ender chest rather than a personal inventory. Fill level and food tooltip reflect the ender chest's food contents across all 27 slots.

### Gluttonous Ender Lunchbox

Identical to the Ender Lunchbox, but allows eating even when the player's hunger bar is full.

### Potion Sash

A personal potion storage item. Right-clicking drinks the next potion stored inside. Sneak + right-click to open the inventory and manage its contents.

- **Stackable potions:** potions, suspicious stews, and milk buckets stack up to the configured `max-stack-size` (default: 3) within the sash.
- **Suspicious Stews and Milk:** also can be in your sash, these are "potion-like".
- **Tiers**: There are tiers 1-6 which allow storing more potions per sash.

## Commands

| Command                                         | Description                                                                                                                                       |
|-------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `/simplelunchboxes give <item> [tier] [player]` | Gives a SimpleLunchboxes item. Tier is 1-6 for lunchboxes and the potion sash; ender variants have no tier. Omitting the player targets yourself. |
| `/simplelunchboxes refresh`                     | Refreshes the held item's display name, item model, and glint to match the current configuration.                                                 |
| `/simplelunchboxes reload`                      | Reloads the configuration and locale file.                                                                                                        |

## Permissions

| Permission                | Description                     | Default |
|---------------------------|---------------------------------|---------|
| `simplelunchboxes.give`   | Use `/simplelunchboxes give`.   | `op`    |
| `simplelunchboxes.reload` | Use `/simplelunchboxes reload`. | `op`    |

`/simplelunchboxes refresh` requires no permission and is available to all players.

## Configuration

All items share the same config structure. `item-model` keys accept any namespaced resource location (e.g. `minecraft:stick`, or a custom pack key like `mynamespace:lunchbox_empty`).

```yaml
# SimpleLunchboxes Configuration
# This file is reloaded with /simplelunchboxes reload

items:
  
  lunchbox:
    display-name: <!i><aqua>Lunchbox
    glint: false
    lore:
      - <!i><gray>A trusty companion for long journeys.
    item-model:
      empty: minecraft:bamboo_raft
      low: minecraft:bamboo_raft
      high: minecraft:bamboo_raft
  
  gluttonous-lunchbox:
    display-name: <!i><light_purple>Gluttonous Lunchbox
    glint: true
    lore:
      - <!i><gray>You can <i>always</i> eat more. <i><b>Always</i>.
    item-model:
      empty: minecraft:bamboo_raft
      low: minecraft:bamboo_raft
      high: minecraft:bamboo_raft
  
  ender-lunchbox:
    display-name: <!i><aqua>Ender Lunchbox
    glint: false
    lore:
      - <!i><gray>You feel like you could reach
      - <!i><gray>into the void to grab a snack.
    item-model:
      empty: minecraft:bamboo_raft
      low: minecraft:bamboo_raft
      high: minecraft:bamboo_raft
  
  gluttonous-ender-lunchbox:
    display-name: <!i><light_purple>Gluttonous Ender Lunchbox
    glint: true
    lore:
      - <!i>Boundless hunger, boundless void.
    item-model:
      empty: minecraft:bamboo_raft
      low: minecraft:bamboo_raft
      high: minecraft:bamboo_raft
  
  potion-sash:
    display-name: <!i>Potion Sash
    glint: false
    lore:
      - <!i><gray>A stylish sash loaded with
      - <!i><gray>potions for any occasion.
    max-stack-size: 3
    item-model:
      empty: minecraft:bundle
      low: minecraft:bundle
      high: minecraft:bundle
```

### Display Names and Lore

All `display-name` and `lore` strings support [MiniMessage](https://docs.advntr.dev/minimessage/) formatting. `<!i>` removes the italic formatting that Minecraft applies to custom item names by default.

### Item Models

Each item has three model states selected by fill fraction, calculated the same way a comparator reads a container:

```
fill = sum(stack_count / max_stack_size) / total_slots
```

| Fill      | Model key used |
|-----------|----------------|
| 0 (empty) | `empty`        |
| < 50%     | `low`          |
| \>= 50%   | `high`         |

Only food / potion items count toward lunchbox / potion sash fill.

After changing item models in config, run `/simplelunchboxes reload` then `/simplelunchboxes refresh` on any existing items to apply the new models.

### Potion Sash Stack Size

`max-stack-size` controls how many identical potions, suspicious stews, or milk buckets can occupy a single slot in the sash. Remainder items (glass bottles, bowls, buckets) always use their vanilla stack size.

## Resource Pack Integration

SimpleLunchboxes uses the `ITEM_MODEL` data component to assign models. Set your `item-model` keys to model paths defined in your resource pack:

```yaml
item-model:
  empty: "mypack:lunchbox/empty"
  low:   "mypack:lunchbox/low"
  high:  "mypack:lunchbox/high"
```
