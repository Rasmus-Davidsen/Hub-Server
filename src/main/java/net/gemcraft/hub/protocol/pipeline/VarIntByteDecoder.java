package net.gemcraft.hub.protocol.pipeline;

import io.netty.util.ByteProcessor;
import org.jetbrains.annotations.NotNull;

public class VarIntByteDecoder implements ByteProcessor {
    private int readVarInt;
    private int bytesRead;
    private @NotNull Result result = Result.TOO_SHORT;

    @Override
    public boolean process(final byte value) {
        readVarInt |= (value & 0x7F) << bytesRead++ * 7;

        if(bytesRead > 3) {
            result = Result.TOO_LONG;
            return false;
        }

        if((value & 0x80) != 128) {
            result = Result.SUCCESS;
            return false;
        }

        return true;
    }

    public int getReadVarInt() {
        return readVarInt;
    }

    public int getBytesRead() {
        return bytesRead;
    }

    public @NotNull Result getResult() {
        return result;
    }

    public enum Result {
        SUCCESS, TOO_SHORT, TOO_LONG
    }
}
