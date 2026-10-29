package net.gemcraft.hub.world.entity;

import net.gemcraft.hub.protocol.ByteMessage;
import org.jetbrains.annotations.NotNull;

public interface DataComponentHolder {
    void encodeData(final @NotNull ByteMessage msg);

    interface DataComponent {
        int getIndex();

        @NotNull DataType getType();
    }

    enum DataType {
        BYTE(0),
        OPTIONAL_TEXT_COMPONENT(6),
        SLOT(7),
        BOOLEAN(8),
        VECTOR_3(39),
        RESOLVABLE_PROFILE(41),
        HUMANOID_ARM(42);

        private final int id;

        DataType(final int id) {
            this.id = id;
        }

        public int getID() {
            return id;
        }
    }
}
