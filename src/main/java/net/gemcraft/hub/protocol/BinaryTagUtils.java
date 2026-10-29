package net.gemcraft.hub.protocol;

import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BinaryTagUtils {
    public static @NotNull BinaryTag create(final @NotNull Component component) {
        final GsonComponentSerializer gson = GsonComponentSerializer.gson();

        final JsonElement json = JsonParser.parseString(
                gson.serialize(component)
        );

        if(json.isJsonObject()) {
            final JsonObject jsonObj = json.getAsJsonObject();

            if(jsonObj.has("extra") && jsonObj.get("extra").isJsonArray()) {
                // Ensure the same JSON type for all extra elements
                JsonArray extraArray = jsonObj
                        .get("extra")
                        .getAsJsonArray();

                extraArray = homogenizeJsonArray(
                        extraArray
                );

                // Remove the extra components for the original
                jsonObj.remove(
                        "extra"
                );

                // Combine the original and the extra components
                final JsonArray combined = new JsonArray();

                combined.add(
                        jsonObj
                );

                combined.addAll(
                        extraArray
                );

                return BinaryTagUtils.create(
                        combined
                );
            }
        }

        return BinaryTagUtils.create(json);
    }

    private static @NotNull JsonArray homogenizeJsonArray(final @NotNull JsonArray array) {
        final JsonArray json = new JsonArray();

        for(final JsonElement element : array) {
            if(!element.isJsonPrimitive()) {
                json.add(element);
                continue;
            }

            final JsonObject wrapper = new JsonObject();

            wrapper.add("text", element);

            json.add(wrapper);
        }

        return json;
    }

    public static @NotNull BinaryTag create(final @NotNull JsonElement json) {
        return switch(json) {
            case final JsonPrimitive jsonPrim -> createJsonPrimitive(
                    json, jsonPrim
            );
            case final JsonObject jsonObj -> createJsonObject(jsonObj);
            case final JsonArray jsonArr -> createJsonArray(jsonArr);
            case final JsonNull _ -> EndBinaryTag.endBinaryTag();
            default -> throw new IllegalArgumentException(
                    "Unknown JSON element: " + json
            );
        };
    }

    private static @NotNull BinaryTag createJsonPrimitive(final @NotNull JsonElement json, final @NotNull JsonPrimitive jsonPrimitive) {
        if(jsonPrimitive.isNumber()) {
            final Number number = json.getAsNumber();

            if(number instanceof final Byte n) {
                return ByteBinaryTag.byteBinaryTag(n);
            }

            if(number instanceof final Short n) {
                return ShortBinaryTag.shortBinaryTag(n);
            }

            if(number instanceof final Integer n) {
                return IntBinaryTag.intBinaryTag(n);
            }

            if(number instanceof final Long n) {
                return LongBinaryTag.longBinaryTag(n);
            }

            if(number instanceof final Float n) {
                return FloatBinaryTag.floatBinaryTag(n);
            }

            if(number instanceof final Double n) {
                return DoubleBinaryTag.doubleBinaryTag(n);
            }

            if(number instanceof LazilyParsedNumber n) {
                return lazilyParsedNumber(n);
            }

            throw new IllegalArgumentException(
                    "Unknown JSON primitive number: " + jsonPrimitive
            );
        }

        if(jsonPrimitive.isString()) {
            return StringBinaryTag.stringBinaryTag(
                    jsonPrimitive.getAsString()
            );
        }

        if(jsonPrimitive.isBoolean()) {
            final byte value = jsonPrimitive.getAsBoolean()
                    ? (byte) 1 : (byte) 0;

            return ByteBinaryTag.byteBinaryTag(value);
        }

        throw new IllegalArgumentException(
                "Unknown JSON primitive: " + jsonPrimitive
        );
    }

    private static @NotNull BinaryTag lazilyParsedNumber(final @NotNull LazilyParsedNumber n) {
        // Double or float if decimal separator
        if(n.toString().contains(".")) {
            final double d = n.doubleValue();

            // Float if precision is preserved
            final float f = (float) d;

            if(Double.compare(d, f) == 0) {
                return FloatBinaryTag.floatBinaryTag(f);
            }

            // Double otherwise
            return DoubleBinaryTag.doubleBinaryTag(d);
        }

        // Integer if within the range
        final long i = n.longValue();

        if(i >= Integer.MIN_VALUE && i <= Integer.MAX_VALUE) {
            return IntBinaryTag.intBinaryTag((int) i);
        }

        // Long otherwise
        return LongBinaryTag.longBinaryTag(i);
    }

    private static @NotNull BinaryTag createJsonObject(final @NotNull JsonObject jsonObject) {
        final CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();

        for(final Map.Entry<String, JsonElement> property : jsonObject.entrySet()) {
            final String key = property.getKey();

            final BinaryTag tag = create(
                    property.getValue()
            );

            builder.put(key, tag);
        }

        return builder.build();
    }

    private static @NotNull BinaryTag createJsonArray(final @NotNull JsonArray jsonArray) {
        final List<JsonElement> jsonList = jsonArray.asList();

        if(jsonList.isEmpty()) {
            return ListBinaryTag.listBinaryTag(
                    EndBinaryTag.endBinaryTag().type(),
                    Collections.emptyList()
            );
        }

        final BinaryTagType<ByteBinaryTag> tagByteType = ByteBinaryTag.ZERO.type();
        final BinaryTagType<IntBinaryTag> tagIntType = IntBinaryTag.intBinaryTag(0).type();
        final BinaryTagType<LongBinaryTag> tagLongType = LongBinaryTag.longBinaryTag(0).type();

        final BinaryTag binaryTag = create(
                jsonList.getFirst()
        );

        final BinaryTagType<? extends BinaryTag> listType = binaryTag.type();
        final int size = jsonList.size();

        if(listType.equals(tagByteType)) {
            final byte[] bytes = new byte[size];

            for(int i = 0; i < bytes.length; i++) {
                bytes[i] = jsonList.get(i).getAsByte();
            }

            return ByteArrayBinaryTag.byteArrayBinaryTag(bytes);
        }

        if(listType.equals(tagIntType)) {
            final int[] ints = new int[size];

            for(int i = 0; i < ints.length; i++) {
                ints[i] = jsonList.get(i).getAsInt();
            }

            return IntArrayBinaryTag.intArrayBinaryTag(ints);
        }

        if(listType.equals(tagLongType)) {
            final long[] longs = new long[size];

            for(int i = 0; i < longs.length; i++) {
                longs[i] = jsonList.get(i).getAsLong();
            }

            return LongArrayBinaryTag.longArrayBinaryTag(longs);
        }

        final List<BinaryTag> tagItems = new ArrayList<>(
                jsonList.size()
        );

        for(final JsonElement element : jsonList) {
            final BinaryTag subTag = create(
                    element
            );

            if(subTag.type() != listType) {
                throw new IllegalArgumentException(
                        "Can't convert mixed JsonArray to Tag"
                );
            }

            tagItems.add(subTag);
        }

        return ListBinaryTag.listBinaryTag(
                listType, tagItems
        );
    }
}
