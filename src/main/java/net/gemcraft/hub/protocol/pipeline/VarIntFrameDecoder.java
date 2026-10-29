package net.gemcraft.hub.protocol.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.gemcraft.hub.Hub;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VarIntFrameDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(final @NotNull ChannelHandlerContext ctx, final @NotNull ByteBuf in, final @NotNull List<@NotNull Object> out) {
        if(!ctx.channel().isActive()) {
            in.clear();
            return;
        }

        final VarIntByteDecoder reader = new VarIntByteDecoder();

        final int varIntEnd = in.forEachByte(
                reader
        );

        if(varIntEnd == -1) {
            return;
        }

        if(reader.getResult() != VarIntByteDecoder.Result.SUCCESS) {
            if(reader.getResult() == VarIntByteDecoder.Result.TOO_LONG) {
                Hub.getLogger().warn(
                        "Data was too long"
                );
            }

            return;
        }

        final int readVarInt = reader.getReadVarInt();
        final int bytesRead = reader.getBytesRead();

        if(readVarInt < 0) {
            Hub.getLogger().warn(
                    "Data had a bad length"
            );

            return;
        }

        if(readVarInt == 0) {
            in.readerIndex(
                    varIntEnd + 1
            );

            return;
        }

        final int minRead = bytesRead + readVarInt;

        if(!in.isReadable(minRead)) {
            return;
        }

        out.add(in.retainedSlice(
                varIntEnd + 1, readVarInt
        ));

        in.skipBytes(
                minRead
        );
    }
}
