# Perpetuity

This mod changes how durability is gonna work, instead of your items being destroyed, they now become Remnants!

## What is it?
Remnants are an unusable form of tools that can be repaired (keeping all components and data, of course) in an anvil (or a few other ways) with specific resources. While modded items aren’t supported by default, I’ve made it really easy for you to either add new remnant “types” or append your items to pre-existing ones entirely through datapacks.

## How do I add my own Item?
To append an item to a remnant type:
```json
// data/modid/remnant/<anything>.json

{
    "appends": {
        "perpetuity:diamond_remnant": [
            "modid:myitem"
        ]
    }
}
```
To create a new remnant type:
```json
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