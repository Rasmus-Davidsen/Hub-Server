package net.gemcraft.hub.world.entity;

/**
 * @apiNote ID is from 'generated/reports/registries.json' under  'minecraft:entity_type'
 **/
public enum EntityType {
    PLAYER(159),
    MANNEQUIN(84);

    private final int id;

    EntityType(final int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }
}
