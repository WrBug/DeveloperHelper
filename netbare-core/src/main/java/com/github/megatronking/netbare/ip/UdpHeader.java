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
package com.github.megatronking.netbare.ip;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;

/**
 * The UDP module  must be able to determine the source and destination internet addresses and
 * the protocol field from the internet header.
 *
 * UDP Header Format:
 *
 * 0      7 8     15 16    23 24    31
 * +--------+--------+--------+--------+
 * |     Source      |   Destination   |
 * |      Port       |      Port       |
 * +--------+--------+--------+--------+
 * |                 |                 |
 * |     Length      |    Checksum     |
 * +--------+--------+--------+--------+
 * |
 * |          data octets ...
 * +---------------- ...
 *
 * See https://tools.ietf.org/html/rfc768
 *
 * @author Megatron King
 * @since 2018-10-10 23:04
 */
public class UdpHeader extends Header {

    private static final short OFFSET_SRC_PORT = 0;
    private static final short OFFSET_DEST_PORT = 2;
    private static final short OFFSET_TLEN = 4;
    private static final short OFFSET_CRC = 6;

    private IpHeader mIpHeader;

    public UdpHeader(IpHeader header, byte[] packet, int offset) {
        super(packet, offset);
        mIpHeader = header;
    }

    public IpHeader getIpHeader() {
        return mIpHeader;
    }

    public short getSourcePort() {
        return readShort(offset + OFFSET_SRC_PORT);
    }

    public void setSourcePort(short port) {
        writeShort(port, offset + OFFSET_SRC_PORT);
    }

    public short getDestinationPort() {
        return readShort(offset + OFFSET_DEST_PORT);
    }

    public void setDestinationPort(short port) {
        writeShort(port, offset + OFFSET_DEST_PORT);
    }

    public short getCrc() {
        return readShort(offset + OFFSET_CRC);
    }

    public void setCrc(short crc) {
        writeShort(crc, offset + OFFSET_CRC);
    }

    public int getHeaderLength() {
        return 8;
    }

    public int getTotalLength() {
        return readShort(offset + OFFSET_TLEN) & 0xFFFF;
    }

    public void setTotalLength(short len) {
        writeShort(len, offset + OFFSET_TLEN);
    }

    public void updateChecksum() {
        setCrc((short) 0);
        setCrc(computeChecksum());
    }

    private short computeChecksum() {
        // Sum = Ip Sum(Source Address + Destination Address) + Protocol + UDP Length
        // Checksum is the 16-bit one's complement of the one's complement sum of a
        // pseudo header of information from the IP header, the UDP header, and the
        // data,  padded  with zero octets  at the end (if  necessary)  to  make  a
        // multiple of two octets.
        int dataLength = mIpHeader.getDataLength();
        long sum = mIpHeader.getIpSum();
        sum += mIpHeader.getProtocol() & 0xFF;
        sum += dataLength;
        sum += getSum(offset, dataLength);
        while ((sum >> 16) > 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }
        return (short) ~sum;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%d -> %d", getSourcePort() & 0xFFFF,
                getDestinationPort() & 0xFFFF);
    }

    public UdpHeader copy() {
        byte[] copyArray = Arrays.copyOf(packet, packet.length);
        IpHeader ipHeader = new IpHeader(copyArray, 0);
        return new UdpHeader(ipHeader, copyArray, offset);
    }

    public ByteBuffer data() {
        int size = mIpHeader.getDataLength() - getHeaderLength();
        int dataOffset = mIpHeader.getHeaderLength() + getHeaderLength();
        byte[] data = new byte[size];
        System.arraycopy(packet, dataOffset, data, 0, size);
        return ByteBuffer.wrap(data);
    }

    public ByteBuffer buffer() {
        return ByteBuffer.wrap(packet, 0, mIpHeader.getTotalLength());
    }

}
