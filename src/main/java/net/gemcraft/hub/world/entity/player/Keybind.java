package net.gemcraft.hub.world.entity.player;

import org.jetbrains.annotations.NotNull;

public enum Keybind {
    ACTION_ATTACK("key.attack"),
    ACTION_INTERACT("key.use"),
    ACTION_DROP_ITEM("key.drop"),
    ACTION_SWAP_HANDS("key.swapOffhand"),
    ACTION_SHOW_PLAYER_LIST("key.playerlist"),
    ACTION_TAKE_SCREENSHOT("key.screenshot"),
    ACTION_PICK_ITEM("key.pickItem"),
    ACTION_HIGHLIGHT_PLAYERS("key.spectatorOutlines"),
    ACTION_SAVE_TOOLBAR("key.saveToolbarActivator"),
    ACTION_LOAD_TOOLBAR("key.loadToolbarActivator"),

    MOVE_FORWARD("key.forward"),
    MOVE_LEFT("key.left"),
    MOVE_BACK("key.back"),
    MOVE_RIGHT("key.right"),
    MOVE_SPRINT("key.sprint"),
    MOVE_SNEAK("key.sneak"),
    MOVE_JUMP("key.jump"),

    TOGGLE_PERSPECTIVE("key.togglePerspective"),
    TOGGLE_FULLSCREEN("key.fullscreen"),
    TOGGLE_CINEMATIC_CAMERA("key.smoothCamera"),

    OPEN_CHAT("key.chat"),
    OPEN_COMMAND("key.command"),
    OPEN_SOCIAL_INTERACTIONS("key.socialInteractions"),
    OPEN_INVENTORY("key.inventory"),
    OPEN_ADVANCEMENTS("key.advancements"),

    HOTBAR_SLOT_1("key.hotbar.1"),
    HOTBAR_SLOT_2("key.hotbar.2"),
    HOTBAR_SLOT_3("key.hotbar.3"),
    HOTBAR_SLOT_4("key.hotbar.4"),
    HOTBAR_SLOT_5("key.hotbar.5"),
    HOTBAR_SLOT_6("key.hotbar.6"),
    HOTBAR_SLOT_7("key.hotbar.7"),
    HOTBAR_SLOT_8("key.hotbar.8"),
    HOTBAR_SLOT_9("key.hotbar.9");

    private final @NotNull String key;

    Keybind(final @NotNull String key) {
        this.key = key;
    }

    public @NotNull String getKey() {
        return key;
    }
}
