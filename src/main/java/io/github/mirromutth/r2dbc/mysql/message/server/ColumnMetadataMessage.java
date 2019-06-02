/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.mirromutth.r2dbc.mysql.message.server;

import io.github.mirromutth.r2dbc.mysql.util.CodecUtils;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static io.github.mirromutth.r2dbc.mysql.util.AssertUtils.requireNonNegative;
import static io.github.mirromutth.r2dbc.mysql.util.AssertUtils.requireNonNull;
import static io.github.mirromutth.r2dbc.mysql.util.AssertUtils.requirePositive;

/**
 * A message of column definition metadata.
 */
public final class ColumnMetadataMessage {

    private static final int MIN_SIZE = 20;

    private final String database;

    private final String tableName;

    private final String originTableName;

    private final String name;

    private final String originName;

    private final int collationId;

    private final int size;

    private final int type;

    private final short definitions;

    private final short decimals;

    private ColumnMetadataMessage(
        String database,
        String tableName,
        String originTableName,
        String name,
        String originName,
        int collationId,
        int size,
        int type,
        short definitions,
        short decimals
    ) {
        this.database = requireNonNull(database, "database must not be null");
        this.tableName = requireNonNull(tableName, "tableName must not be null");
        this.originTableName = requireNonNull(originTableName, "originTableName must not be null");
        this.name = requireNonNull(name, "name must not be null");
        this.originName = requireNonNull(originName, "originName must not be null");
        this.collationId = requirePositive(collationId, "collationId must be a positive integer");
        this.size = requireNonNegative(size, "size must not be negative");
        this.type = requireNonNegative(type, "type must not be negative");
        this.definitions = definitions;
        this.decimals = decimals;
    }

    public String getDatabase() {
        return database;
    }

    public String getTableName() {
        return tableName;
    }

    public String getOriginTableName() {
        return originTableName;
    }

    public String getName() {
        return name;
    }

    public String getOriginName() {
        return originName;
    }

    public int getCollationId() {
        return collationId;
    }

    public int getSize() {
        return size;
    }

    public int getType() {
        return type;
    }

    public short getDefinitions() {
        return definitions;
    }

    public short getDecimals() {
        return decimals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColumnMetadataMessage)) return false;

        ColumnMetadataMessage that = (ColumnMetadataMessage) o;

        if (collationId != that.collationId) return false;
        if (size != that.size) return false;
        if (type != that.type) return false;
        if (definitions != that.definitions) return false;
        if (decimals != that.decimals) return false;
        if (!database.equals(that.database)) return false;
        if (!tableName.equals(that.tableName)) return false;
        if (!originTableName.equals(that.originTableName)) return false;
        if (!name.equals(that.name)) return false;
        return originName.equals(that.originName);
    }

    @Override
    public int hashCode() {
        int result = database.hashCode();
        result = 31 * result + tableName.hashCode();
        result = 31 * result + originTableName.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + originName.hashCode();
        result = 31 * result + collationId;
        result = 31 * result + size;
        result = 31 * result + type;
        result = 31 * result + (int) definitions;
        result = 31 * result + (int) decimals;
        return result;
    }

    @Override
    public String toString() {
        return "ColumnMetadataMessage{" +
                "database='" + database + '\'' +
                ", tableName='" + tableName + '\'' +
                ", originTableName='" + originTableName + '\'' +
                ", name='" + name + '\'' +
                ", originName='" + originName + '\'' +
                ", collationId=" + collationId +
                ", size=" + size +
                ", type=" + type +
                ", definitions=" + definitions +
                ", decimals=" + decimals +
                '}';
    }

    static boolean isLooksLike(ByteBuf buf) {
        int bufSize = buf.readableBytes();

        if (bufSize < MIN_SIZE) {
            return false;
        }

        int readerIndex = buf.readerIndex();
        byte first = buf.getByte(readerIndex);

        if (first != 3) {
            return false;
        }

        // "def".equals(buf.toString(readerIndex + 1, 3, StandardCharsets.US_ASCII))
        return 'd' == buf.getByte(readerIndex + 1) &&
            'e' == buf.getByte(readerIndex + 2) &&
            'f' == buf.getByte(readerIndex + 3);
    }

    static ColumnMetadataMessage decode(ByteBuf buf, Charset charset) {
        buf.skipBytes(4); // "def" which sized by var integer

        String database = CodecUtils.readVarIntSizedString(buf, charset);
        String tableName = CodecUtils.readVarIntSizedString(buf, charset);
        String originTableName = CodecUtils.readVarIntSizedString(buf, charset);
        String name = CodecUtils.readVarIntSizedString(buf, charset);
        String originName = CodecUtils.readVarIntSizedString(buf, charset);

        CodecUtils.readVarInt(buf); // skip constant 0x0c encoded by var integer

        int collationId = buf.readUnsignedShortLE();
        int size = buf.readIntLE();
        short type = buf.readUnsignedByte();
        short definition = buf.readShortLE();

        return new ColumnMetadataMessage(
            database,
            tableName,
            originTableName,
            name,
            originName,
            collationId,
            size,
            type,
            definition,
            buf.readUnsignedByte()
        );
    }
}