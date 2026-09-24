# Perpetuity

Perpetuity is a mod that adds some balancing changes to enchanting and experience, designed to work hand-in-hand with Enchancement (although the mod works great by itself too!).

## Remnants

Never lose your items again! Instead of items being destroyed when broken, they become remnants, and can be repaired at an anvil with any resource compatible with the remnant.

## Renovite

Renovite is a new resource crafted with amethyst clusters and experience bottles **(which you can create by shift-right clicking normal bottles!)**.

It seems to have some interesting healing properties...

### Universal repairing

Renovite can be used in an anvil to repair both remnants and damaged tools, regardless of ingredient requirement.

### Renovite Pylons

With a _lot_ of renovite and a little bit more amethyst, you can create a Renovite Pylon, a special block which **mends tools in nearby player inventories** every few seconds.

## Experience Cakes
With some experience bottles and (most) of a cake's ingredients, you can create an Experience Cake, which is capable of repairing **seven remnants** for **1/7th of their durability**.

# For Developers
### Adding a new Remnant type
```json5
// data/modid/remnant/<type id>.json

{
    // Should point to a translation key
    "name": "item.remnant.diamond",
    // The item's texture, see below for examples
    "texture": "perpetuity:item/diamond_remnant",
    // Items used to repair this remnant type
    "repair_items": [ 
      "minecraft:diamond"
    ],
    // Items that will turn into this remnant upon breaking
    "items": [
        "minecraft:diamond_sword",
        "minecraft:diamond_pickaxe"
    ]
}
```
### Appending items to existing Remnants
It is worth mentioning that this _does_ work with remnants from other mods, but of course, the other mod must be installed. If the remnant type cannot be found, Perpetuity will silently ignore the appended item(s).
```json5
// data/modid/remnant/<anything>.json

{
    "appends": {
        "perpetuity:diamond_remnant": [
            "modid:myitem"
        ]
    }
}
```
### Making Renovite Pylons ineffective on items
```json5
// data/perpetuity/tags/item/ignored_by_pylon.json

{
  "replace": false,
  "values": [
    "mymod:myitem"
  ]
}
```
### Making an item unrepairable with Renovite
This will not make its remnant form unrepairable.
```json5
// data/perpetuity/tags/item/unrepairable_with_renovite.json

{
  "replace": false,
  "values": [
    "mymod:myitem"
  ]
}
```