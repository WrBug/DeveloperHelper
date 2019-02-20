/*  NetBare - An android network capture and injection library.
 *  Copyright (C) 2018-2019 Megatron King
 *  Copyright (C) 2018-2019 GuoShi
 *
 *  NetBare is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU General Public License as published by the Free Software Found-
 *  ation, either version 3 of the License, or (at your option) any later version.
 *
 *  NetBare is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with NetBare.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Copyright (C) 2013 Square, Inc.
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
package com.github.megatronking.netbare.http2;

import android.text.TextUtils;

import com.github.megatronking.netbare.NetBareUtils;
import com.github.megatronking.netbare.http.HttpMethod;
import com.github.megatronking.netbare.http.HttpProtocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read and write HPACK v10.
 *
 * https://httpwg.org/specs/rfc7540.html#HeaderBlock
 *
 * This implementation uses an array for the dynamic table and a list for indexed entries.  Dynamic
 * entries are added to the array, starting in the last position moving forward.  When the array
 * fills, it is doubled.
 *
 * @author Megatron King
 * @since 2019/1/5 20:13
 */
/* package */ final class Hpack {

    private static final int PREFIX_4_BITS = 0x0f;
    private static final int PREFIX_5_BITS = 0x1f;
    private static final int PREFIX_6_BITS = 0x3f;
    private static final int PREFIX_7_BITS = 0x7f;

    private static final Header[] STATIC_HEADER_TABLE = new Header[] {
            new Header(Header.TARGET_AUTHORITY, ""),
            new Header(Header.TARGET_METHOD, "GET"),
            new Header(Header.TARGET_METHOD, "POST"),
            new Header(Header.TARGET_PATH, "/"),
            new Header(Header.TARGET_PATH, "/index.html"),
            new Header(Header.TARGET_SCHEME, "http"),
            new Header(Header.TARGET_SCHEME, "https"),
            new Header(Header.RESPONSE_STATUS, "200"),
            new Header(Header.RESPONSE_STATUS, "204"),
            new Header(Header.RESPONSE_STATUS, "206"),
            new Header(Header.RESPONSE_STATUS, "304"),
            new Header(Header.RESPONSE_STATUS, "400"),
            new Header(Header.RESPONSE_STATUS, "404"),
            new Header(Header.RESPONSE_STATUS, "500"),
            new Header("accept-charset", ""),
            new Header("accept-encoding", "gzip, deflate"),
            new Header("accept-language", ""),
            new Header("accept-ranges", ""),
            new Header("accept", ""),
            new Header("access-control-allow-origin", ""),
            new Header("age", ""),
            new Header("allow", ""),
            new Header("authorization", ""),
            new Header("cache-control", ""),
            new Header("content-disposition", ""),
            new Header("content-encoding", ""),
            new Header("content-language", ""),
            new Header("content-length", ""),
            new Header("content-location", ""),
            new Header("content-range", ""),
            new Header("content-type", ""),
            new Header("cookie", ""),
            new Header("date", ""),
            new Header("etag", ""),
            new Header("expect", ""),
            new Header("expires", ""),
            new Header("from", ""),
            new Header("host", ""),
            new Header("if-match", ""),
            new Header("if-modified-since", ""),
            new Header("if-none-match", ""),
            new Header("if-range", ""),
            new Header("if-unmodified-since", ""),
            new Header("last-modified", ""),
            new Header("link", ""),
            new Header("location", ""),
            new Header("max-forwards", ""),
            new Header("proxy-authenticate", ""),
            new Header("proxy-authorization", ""),
            new Header("range", ""),
            new Header("referer", ""),
            new Header("refresh", ""),
            new Header("retry-after", ""),
            new Header("server", ""),
            new Header("set-cookie", ""),
            new Header("strict-transport-security", ""),
            new Header("transfer-encoding", ""),
            new Header("user-agent", ""),
            new Header("vary", ""),
            new Header("via", ""),
            new Header("www-authenticate", "")
    };

    private static final List<String> HTTP_2_SKIPPED_REQUEST_HEADERS = Arrays.asList(
            "connection",
            "host",
            "keep_alive",
            "proxy_connection",
            "te",
            "transfer_encoding",
            "encoding",
            "upgrade");

    private static final List<String> HTTP_2_SKIPPED_RESPONSE_HEADERS = Arrays.asList(
            "connection",
            "host",
            "keep_alive",
            "proxy_connection",
            "te",
            "transfer_encoding",
            "encoding",
            "upgrade");

    private static final Map<String, Integer> NAME_TO_FIRST_INDEX = nameToFirstIndex();

    private static Map<String, Integer> nameToFirstIndex() {
        Map<String, Integer> result = new LinkedHashMap<>(STATIC_HEADER_TABLE.length);
        for (int i = 0; i < STATIC_HEADER_TABLE.length; i++) {
            if (!result.containsKey(STATIC_HEADER_TABLE[i].name)) {
                result.put(STATIC_HEADER_TABLE[i].name, i);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private static final int DEFAULT_HEADER_TABLE_SIZE_SETTING = 4096;
    private static final int SETTINGS_HEADER_TABLE_SIZE_LIMIT = 16384;

    static final class Reader {

        private final List<Header> mHeaders;

        private int mHeaderTableSizeSetting;

        private Header[] mDynamicTable;
        private int mNextHeaderIndex;
        private int mHeaderCount;
        private int mDynamicTableByteCount;

        private int mMaxDynamicTableByteCount;

        /* package */ Reader() {
            this.mHeaders = new ArrayList<>();
            this.mDynamicTable = new Header[8];
            this.mNextHeaderIndex = mDynamicTable.length - 1;
            this.mMaxDynamicTableByteCount = DEFAULT_HEADER_TABLE_SIZE_SETTING;
            this.mHeaderTableSizeSetting = DEFAULT_HEADER_TABLE_SIZE_SETTING;
        }

        void setHeaderTableSizeSetting(int headerTableSizeSetting) {
            if (mHeaderTableSizeSetting == headerTableSizeSetting) {
                return;
            }
            this.mHeaderTableSizeSetting = headerTableSizeSetting;
            int effectiveHeaderTableSize = Math.min(headerTableSizeSetting,
                    SETTINGS_HEADER_TABLE_SIZE_LIMIT);

            if (mMaxDynamicTableByteCount == effectiveHeaderTableSize) {
                return; // No change.
            }
            mMaxDynamicTableByteCount = effectiveHeaderTableSize;
            adjustDynamicTableByteCount();
        }

        void readHeaders(ByteBuffer buffer, byte flags, DecodeCallback callback) throws IOException,
                IndexOutOfBoundsException {
            mHeaders.clear();
            while (buffer.hasRemaining()) {
                int b = buffer.get() & 0xff;
                if (b == 0x80) { // 10000000
                    throw new IOException("Hpack read headers failed: index == 0");
                } else if ((b & 0x80) == 0x80) { // 1NNNNNNN
                    int index = readInt(buffer, b, PREFIX_7_BITS);
                    readIndexedHeader(index - 1);
                } else if (b == 0x40) { // 01000000
                    readLiteralHeaderWithIncrementalIndexingNewName(buffer);
                } else if ((b & 0x40) == 0x40) {  // 01NNNNNN
                    int index = readInt(buffer, b, PREFIX_6_BITS);
                    readLiteralHeaderWithIncrementalIndexingIndexedName(buffer, index - 1);
                } else if ((b & 0x20) == 0x20) {  // 001NNNNN
                    mMaxDynamicTableByteCount = readInt(buffer, b, PREFIX_5_BITS);
                    if (mMaxDynamicTableByteCount < 0
                            || mMaxDynamicTableByteCount > mHeaderTableSizeSetting) {
                        throw new IOException("Hpack read headers failed: Invalid dynamic table " +
                                "size update " + mMaxDynamicTableByteCount);
                    }
                    adjustDynamicTableByteCount();
                } else if (b == 0x10 || b == 0) { // 000?0000 - Ignore never indexed bit.
                    readLiteralHeaderWithoutIndexingNewName(buffer);
                } else { // 000?NNNN - Ignore never indexed bit.
                    int index = readInt(buffer, b, PREFIX_4_BITS);
                    readLiteralHeaderWithoutIndexingIndexedName(buffer, index - 1);
                }
            }
            // Build normal http header part
            String method = null;
            String path = null;
            String host = null;
            String status = null;
            List<Header> headers = new ArrayList<>();
            for (Header header : mHeaders) {
                if (header.name.equals(Header.TARGET_METHOD)) {
                    method = header.value;
                } else if (header.name.equals(Header.TARGET_PATH)) {
                    path = header.value;
                } else if (header.name.equals(Header.TARGET_AUTHORITY)) {
                    host = header.value;
                } else if (header.name.equalsIgnoreCase("host")) {
                    host = header.value;
                } else if (header.name.equalsIgnoreCase(Header.RESPONSE_STATUS)) {
                    status = header.value;
                } else {
                    headers.add(header);
                }
            }
            StringBuilder sb = new StringBuilder();
            if (method != null && path != null) {
                sb.append(method).append(" ").append(path).append(" ").append(HttpProtocol.HTTP_2.toString());
                sb.append(NetBareUtils.LINE_END);
            }
            if (status != null) {
                sb.append(HttpProtocol.HTTP_2.toString()).append(" ").append(status);
                sb.append(NetBareUtils.LINE_END);
            }
            if (host != null) {
                headers.add(0, new Header("Host", host));
            }
            for (Header header : headers) {
                if (header.name.equals(Header.TARGET_SCHEME)) {
                    continue;
                }
                sb.append(header.name).append(": ");
                if (header.value != null) {
                    sb.append(header.value);
                }
                sb.append(NetBareUtils.LINE_END);
            }
            if ((flags & Http2.FLAG_END_HEADERS) != 0) {
                sb.append(NetBareUtils.LINE_END);
            }
            callback.onResult(ByteBuffer.wrap(sb.toString().getBytes()),
                    (flags & Http2.FLAG_END_STREAM) != 0);
        }

        private int readInt(ByteBuffer buffer, int firstByte, int prefixMask) {
            int prefix = firstByte & prefixMask;
            if (prefix < prefixMask) {
                return prefix; // This was a single byte value.
            }

            // This is a multibyte value. Read 7 bits at a time.
            int result = prefixMask;
            int shift = 0;
            while (true) {
                int b = buffer.get();
                if ((b & 0x80) != 0) { // Equivalent to (b >= 128) since b is in [0..255].
                    result += (b & 0x7f) << shift;
                    shift += 7;
                } else {
                    result += b << shift; // Last byte.
                    break;
                }
            }
            return result;
        }

        private int readByte(ByteBuffer buffer) {
            return buffer.get() & 0xff;
        }

        private void readIndexedHeader(int index) throws IOException {
            if (isStaticHeader(index)) {
                Header staticEntry = STATIC_HEADER_TABLE[index];
                mHeaders.add(staticEntry);
            } else {
                int dynamicTableIndex = dynamicTableIndex(index - STATIC_HEADER_TABLE.length);
                if (dynamicTableIndex < 0 || dynamicTableIndex >= mDynamicTable.length) {
                    throw new IOException("Hpack read headers failed: Header index too large " +
                            (index + 1));
                }
                Header header = mDynamicTable[dynamicTableIndex];
                if (header == null) {
                    throw new IOException("Hpack read headers failed: read dynamic table failed!");
                }
                mHeaders.add(header);
            }
        }

        private int dynamicTableIndex(int index) {
            return mNextHeaderIndex + 1 + index;
        }

        private boolean isStaticHeader(int index) {
            return index >= 0 && index <= STATIC_HEADER_TABLE.length - 1;
        }

        private void readLiteralHeaderWithoutIndexingIndexedName(ByteBuffer buffer, int index)
                throws IOException {
            String name = getName(index);
            String value = readString(buffer);
            mHeaders.add(new Header(name, value));
        }

        private String getName(int index) throws IOException {
            if (isStaticHeader(index)) {
                return STATIC_HEADER_TABLE[index].name;
            } else {
                int dynamicTableIndex = dynamicTableIndex(index - STATIC_HEADER_TABLE.length);
                if (dynamicTableIndex < 0 || dynamicTableIndex >= mDynamicTable.length) {
                    throw new IOException("Hpack read headers failed: Header index too large " +
                            (index + 1));
                }
                return mDynamicTable[dynamicTableIndex].name;
            }
        }

        private String readString(ByteBuffer buffer) throws IOException {
            int firstByte = readByte(buffer);
            boolean huffmanDecode = (firstByte & 0x80) == 0x80; // 1NNNNNNN
            int length = readInt(buffer, firstByte, PREFIX_7_BITS);
            if (buffer.remaining() < length) {
                throw new IOException("Hpack read headers failed: data not enough, expect: " +
                        length + " actual: " + buffer.remaining());
            }
            byte[] data = new byte[length];
            buffer.get(data);
            return new String(huffmanDecode ? Huffman.get().decode(data) : data);
        }

        private void readLiteralHeaderWithIncrementalIndexingNewName(ByteBuffer buffer)
                throws IOException {
            String name = checkLowercase(readString(buffer));
            String value = readString(buffer);
            insertIntoDynamicTable(-1, new Header(name, value));
        }

        private void readLiteralHeaderWithIncrementalIndexingIndexedName(ByteBuffer buffer,
                                                                         int nameIndex)
                throws IOException {
            String name = getName(nameIndex);
            String value = readString(buffer);
            insertIntoDynamicTable(-1, new Header(name, value));
        }

        private void readLiteralHeaderWithoutIndexingNewName(ByteBuffer buffer) throws IOException {
            String name = checkLowercase(readString(buffer));
            String value = readString(buffer);
            mHeaders.add(new Header(name, value));
        }

        private void insertIntoDynamicTable(int index, Header entry) throws IOException {
            mHeaders.add(entry);

            int delta = entry.hpackSize();
            if (index != -1) { // Index -1 == new header.
                Header header = mDynamicTable[dynamicTableIndex(index)];
                if (header == null) {
                    throw new IOException("Hpack read headers failed: insert dynamic table failed!");
                }
                delta -= header.hpackSize();
            }

            // if the new or replacement header is too big, drop all entries.
            if (delta > mMaxDynamicTableByteCount) {
                clearDynamicTable();
                return;
            }

            // Evict headers to the required length.
            int bytesToRecover = (mDynamicTableByteCount + delta) - mMaxDynamicTableByteCount;
            int entriesEvicted = evictToRecoverBytes(bytesToRecover);

            if (index == -1) { // Adding a value to the dynamic table.
                if (mHeaderCount + 1 > mDynamicTable.length) { // Need to grow the dynamic table.
                    Header[] doubled = new Header[mDynamicTable.length * 2];
                    System.arraycopy(mDynamicTable, 0, doubled, mDynamicTable.length,
                            mDynamicTable.length);
                    mNextHeaderIndex = mDynamicTable.length - 1;
                    mDynamicTable = doubled;
                }
                index = mNextHeaderIndex--;
                mDynamicTable[index] = entry;
                mHeaderCount++;
            } else { // Replace value at same position.
                index += dynamicTableIndex(index) + entriesEvicted;
                mDynamicTable[index] = entry;
            }
            mDynamicTableByteCount += delta;
        }

        private void clearDynamicTable() {
            Arrays.fill(mDynamicTable, null);
            mNextHeaderIndex = mDynamicTable.length - 1;
            mHeaderCount = 0;
            mDynamicTableByteCount = 0;
        }

        private int evictToRecoverBytes(int bytesToRecover) {
            int entriesToEvict = 0;
            if (bytesToRecover > 0) {
                // determine how many headers need to be evicted.
                for (int j = mDynamicTable.length - 1; j >= mNextHeaderIndex && bytesToRecover > 0; j--) {
                    bytesToRecover -= mDynamicTable[j].hpackSize();
                    mDynamicTableByteCount -= mDynamicTable[j].hpackSize();
                    mHeaderCount--;
                    entriesToEvict++;
                }
                System.arraycopy(mDynamicTable, mNextHeaderIndex + 1, mDynamicTable,
                        mNextHeaderIndex + 1 + entriesToEvict, mHeaderCount);
                mNextHeaderIndex += entriesToEvict;
            }
            return entriesToEvict;
        }

        private void adjustDynamicTableByteCount() {
            if (mMaxDynamicTableByteCount < mDynamicTableByteCount) {
                if (mMaxDynamicTableByteCount == 0) {
                    clearDynamicTable();
                } else {
                    evictToRecoverBytes(mDynamicTableByteCount - mMaxDynamicTableByteCount);
                }
            }
        }

        private String checkLowercase(String name) throws IOException {
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    throw new IOException("Hpack read headers failed: mixed case name: " + name);
                }
            }
            return name;
        }

    }

    static final class Writer {

        private int mSmallestHeaderTableSizeSetting;
        private boolean mEmitDynamicTableSizeUpdate;

        private int mHeaderTableSizeSetting;
        private int mMaxDynamicTableByteCount;

        // Visible for testing.
        private Header[] mDynamicTable;
        // Array is populated back to front, so new entries always have lowest index.
        private int mNextHeaderIndex;
        private int mHeaderCount = 0;
        private int mDynamicTableByteCount = 0;

        private ByteArrayOutputStream mOut;

        Writer() {
            this.mDynamicTable = new Header[8];
            this.mNextHeaderIndex = mDynamicTable.length - 1;
            this.mSmallestHeaderTableSizeSetting = Integer.MAX_VALUE;
            this.mHeaderTableSizeSetting = DEFAULT_HEADER_TABLE_SIZE_SETTING;
            this.mMaxDynamicTableByteCount = DEFAULT_HEADER_TABLE_SIZE_SETTING;
        }

        byte[] writeRequestHeaders(HttpMethod method, String path, String host,
                                Map<String, List<String>> headers) throws IOException {
            mOut = new ByteArrayOutputStream();
            List<Header> hpackHeaders = new ArrayList<>();
            hpackHeaders.add(new Header(Header.TARGET_METHOD, method.name()));
            hpackHeaders.add(new Header(Header.TARGET_PATH, path));
            hpackHeaders.add(new Header(Header.TARGET_AUTHORITY, host));
            hpackHeaders.add(new Header(Header.TARGET_SCHEME, "https"));
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (HTTP_2_SKIPPED_REQUEST_HEADERS.contains(entry.getKey().toLowerCase())) {
                    continue;
                }
                for (String value : entry.getValue()) {
                    hpackHeaders.add(new Header(entry.getKey(), value));
                }
            }
            return writeHeaders(hpackHeaders);
        }

        byte[] writeResponseHeaders(int code, String message, Map<String, List<String>> headers)
                throws IOException {
            mOut = new ByteArrayOutputStream();
            List<Header> hpackHeaders = new ArrayList<>();
            hpackHeaders.add(new Header(Header.RESPONSE_STATUS, TextUtils.isEmpty(message) ? String.valueOf(code) :
                    code + " " + message));
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (HTTP_2_SKIPPED_RESPONSE_HEADERS.contains(entry.getKey().toLowerCase())) {
                    continue;
                }
                for (String value : entry.getValue()) {
                    hpackHeaders.add(new Header(entry.getKey(), value));
                }
            }
            return writeHeaders(hpackHeaders);
        }

        private void clearDynamicTable() {
            Arrays.fill(mDynamicTable, null);
            mNextHeaderIndex = mDynamicTable.length - 1;
            mHeaderCount = 0;
            mDynamicTableByteCount = 0;
        }

        private int evictToRecoverBytes(int bytesToRecover) {
            int entriesToEvict = 0;
            if (bytesToRecover > 0) {
                // determine how many headers need to be evicted.
                for (int j = mDynamicTable.length - 1; j >= mNextHeaderIndex && bytesToRecover > 0; j--) {
                    bytesToRecover -= mDynamicTable[j].hpackSize();
                    mDynamicTableByteCount -= mDynamicTable[j].hpackSize();
                    mHeaderCount--;
                    entriesToEvict++;
                }
                System.arraycopy(mDynamicTable, mNextHeaderIndex + 1, mDynamicTable,
                        mNextHeaderIndex + 1 + entriesToEvict, mHeaderCount);
                Arrays.fill(mDynamicTable, mNextHeaderIndex + 1, mNextHeaderIndex + 1 + entriesToEvict, null);
                mNextHeaderIndex += entriesToEvict;
            }
            return entriesToEvict;
        }

        private void insertIntoDynamicTable(Header entry) {
            int delta = entry.hpackSize();

            // if the new or replacement header is too big, drop all entries.
            if (delta > mMaxDynamicTableByteCount) {
                clearDynamicTable();
                return;
            }

            // Evict headers to the required length.
            int bytesToRecover = (mDynamicTableByteCount + delta) - mMaxDynamicTableByteCount;
            evictToRecoverBytes(bytesToRecover);

            if (mHeaderCount + 1 > mDynamicTable.length) { // Need to grow the dynamic table.
                Header[] doubled = new Header[mDynamicTable.length * 2];
                System.arraycopy(mDynamicTable, 0, doubled, mDynamicTable.length, mDynamicTable.length);
                mNextHeaderIndex = mDynamicTable.length - 1;
                mDynamicTable = doubled;
            }
            int index = mNextHeaderIndex--;
            mDynamicTable[index] = entry;
            mHeaderCount++;
            mDynamicTableByteCount += delta;
        }

        private byte[] writeHeaders(List<Header> headerBlock) throws IOException {
            if (mEmitDynamicTableSizeUpdate) {
                if (mSmallestHeaderTableSizeSetting < mMaxDynamicTableByteCount) {
                    // Multiple dynamic table size updates!
                    writeInt(mSmallestHeaderTableSizeSetting, PREFIX_5_BITS, 0x20);
                }
                mEmitDynamicTableSizeUpdate = false;
                mSmallestHeaderTableSizeSetting = Integer.MAX_VALUE;
                writeInt(mMaxDynamicTableByteCount, PREFIX_5_BITS, 0x20);
            }

            for (int i = 0, size = headerBlock.size(); i < size; i++) {
                Header header = headerBlock.get(i);
                String name = header.name.toLowerCase();
                String value = header.value;
                int headerIndex = -1;
                int headerNameIndex = -1;

                Integer staticIndex = NAME_TO_FIRST_INDEX.get(name);
                if (staticIndex != null) {
                    headerNameIndex = staticIndex + 1;
                    if (headerNameIndex > 1 && headerNameIndex < 8) {
                        // Only search a subset of the static header table. Most entries have an empty value, so
                        // it's unnecessary to waste cycles looking at them. This check is built on the
                        // observation that the header entries we care about are in adjacent pairs, and we
                        // always know the first index of the pair.
                        if (TextUtils.equals(STATIC_HEADER_TABLE[headerNameIndex - 1].value, value)) {
                            headerIndex = headerNameIndex;
                        } else if (TextUtils.equals(STATIC_HEADER_TABLE[headerNameIndex].value, value)) {
                            headerIndex = headerNameIndex + 1;
                        }
                    }
                }

                if (headerIndex == -1) {
                    for (int j = mNextHeaderIndex + 1, length = mDynamicTable.length; j < length; j++) {
                        if (TextUtils.equals(mDynamicTable[j].name, name)) {
                            if (TextUtils.equals(mDynamicTable[j].value, value)) {
                                headerIndex = j - mNextHeaderIndex + STATIC_HEADER_TABLE.length;
                                break;
                            } else if (headerNameIndex == -1) {
                                headerNameIndex = j - mNextHeaderIndex + STATIC_HEADER_TABLE.length;
                            }
                        }
                    }
                }

                if (headerIndex != -1) {
                    // Indexed Header Field.
                    writeInt(headerIndex, PREFIX_7_BITS, 0x80);
                } else if (headerNameIndex == -1) {
                    // Literal Header Field with Incremental Indexing - New Name.
                    mOut.write(0x40);
                    writeString(name);
                    writeString(value);
                    insertIntoDynamicTable(header);
                } else if (name.startsWith(Header.PSEUDO_PREFIX) && !Header.TARGET_AUTHORITY.equals(name)) {
                    // Follow Chromes lead - only include the :authority pseudo header, but exclude all other
                    // pseudo headers. Literal Header Field without Indexing - Indexed Name.
                    writeInt(headerNameIndex, PREFIX_4_BITS, 0);
                    writeString(value);
                } else {
                    // Literal Header Field with Incremental Indexing - Indexed Name.
                    writeInt(headerNameIndex, PREFIX_6_BITS, 0x40);
                    writeString(value);
                    insertIntoDynamicTable(header);
                }
            }
            return mOut.toByteArray();
        }

        private void writeInt(int value, int prefixMask, int bits) {
            // Write the raw value for a single byte value.
            if (value < prefixMask) {
                mOut.write(bits | value);
                return;
            }

            // Write the mask to start a multibyte value.
            mOut.write(bits | prefixMask);
            value -= prefixMask;

            // Write 7 bits at a time 'til we're done.
            while (value >= 0x80) {
                int b = value & 0x7f;
                mOut.write(b | 0x80);
                value >>>= 7;
            }
            mOut.write(value);
        }

        private void writeString(String data) throws IOException {
            byte[] stringBytes = data.getBytes();
            if (Huffman.get().encodedLength(data) < stringBytes.length) {
                ByteBuffer buffer = ByteBuffer.allocate(stringBytes.length);
                Huffman.get().encode(data, buffer);
                buffer.flip();
                writeInt(buffer.remaining(), PREFIX_7_BITS, 0x80);
                mOut.write(buffer.array(), buffer.position(), buffer.remaining());
            } else {
                writeInt(stringBytes.length, PREFIX_7_BITS, 0);
                mOut.write(data.getBytes());
            }
        }

        void setHeaderTableSizeSetting(int headerTableSizeSetting) {
            if (mHeaderTableSizeSetting == headerTableSizeSetting) {
                return;
            }
            this.mHeaderTableSizeSetting = headerTableSizeSetting;
            int effectiveHeaderTableSize = Math.min(headerTableSizeSetting,
                    SETTINGS_HEADER_TABLE_SIZE_LIMIT);

            if (mMaxDynamicTableByteCount == effectiveHeaderTableSize) {
                return; // No change.
            }

            if (effectiveHeaderTableSize < mMaxDynamicTableByteCount) {
                mSmallestHeaderTableSizeSetting = Math.min(mSmallestHeaderTableSizeSetting,
                        effectiveHeaderTableSize);
            }
            mEmitDynamicTableSizeUpdate = true;
            mMaxDynamicTableByteCount = effectiveHeaderTableSize;
            adjustDynamicTableByteCount();
        }

        private void adjustDynamicTableByteCount() {
            if (mMaxDynamicTableByteCount < mDynamicTableByteCount) {
                if (mMaxDynamicTableByteCount == 0) {
                    clearDynamicTable();
                } else {
                    evictToRecoverBytes(mDynamicTableByteCount - mMaxDynamicTableByteCount);
                }
            }
        }

    }

    private static final class Header {

        // Special header names defined in HTTP/2 spec.
        private static final String PSEUDO_PREFIX = ":";
        private static final String RESPONSE_STATUS = ":status";
        private static final String TARGET_METHOD = ":method";
        private static final String TARGET_PATH = ":path";
        private static final String TARGET_SCHEME = ":scheme";
        private static final String TARGET_AUTHORITY = ":authority";

        private String name;
        private String value;

        private Header(String name, String value) {
            this.name = name;
            this.value = value;
        }

        private int hpackSize() {
            return 32 + name.getBytes().length + value.getBytes().length;
        }

        @Override
        public String toString() {
            return name + ": " + value;
        }

    }

}
