/*
 * Nexmon PenTestSuite
 * Copyright (C) 2016 Fabian Knapp
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.usslab.decam.Data;

import org.usslab.decam.Util.DataConvert;
import org.usslab.decam.Util.Logg;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;


public class Packet {
    public static final String TAG = "PacketsClass";

    public int _number;
    public int _encap;
    public int _headerLen;
    public int _dataLen;
    public byte[] _rawHeader;
    public byte[] _rawData;

    //public int _band;  // only for ZigBee also since there is no radiotap header


    public final static int WTAP_LINKTYPE_RADIOTAP = 23;
    public final static int WTAP_LINKTYPE_ETHERNET = 1;
    public final static int PCAP_LINKTYPE_RADIOTAP = 127;
    public final static int PCAP_LINKTYPE_ETHERNET = 1;

    public final static int PCAP_VALID_DATALEN = 0x40;

    public final static byte[] bffff0x4 = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff};
    public final static String ffff0x4 = DataConvert.bytesToMac(bffff0x4);

    public final static int VALID_QOS_DATA_MIN_LEN = 68;
    public final static byte flagOfQosFrame = (byte) 0x88;
    public final static byte flagOfQosFrameDsbits = (byte) 0x01;
    public final static byte maskForflagOfQosFrameDsbits = (byte) 0x03;

    private int sequenceCount;
    private int fullLength;
    private long timestamp;
    private byte[] _raw_radioTap;
    private Random random;

    public Packet(int encap) {
        _rawHeader = null;
        _rawData = null;
        _encap = encap;
        //_band = -1;
    }

    public Packet(byte[] data, byte[] header, int sequenceCount) {
        _encap = 23;
        //_band = -1;
        this._rawData = data;
        this._rawHeader = header;
        this._raw_radioTap = Arrays.copyOfRange(data, 0, 30);
        this._headerLen = header.length;
        this._dataLen = data.length;
        this.sequenceCount = sequenceCount;
        random = new Random();

    }


    public boolean isBeacon() {
        try {
            int rtap = getRtapLength();
            return (_rawData.length > rtap && _rawData[rtap] == -128);
        } catch (ArrayIndexOutOfBoundsException e) {
            if (random.nextInt(1000) < 20) {
                Logg.e(TAG, "isBeacon Error", e);
            }
            return false;
        }

    }

    public String getSSIDfromBeacon() {
        int rtap = getRtapLength();
        if (_rawData.length >= (rtap + 38)) {
            int ssidLength = _rawData[rtap + 37];
            byte[] ssidByte = new byte[ssidLength];

            for (int i = rtap + 38; i < (rtap + 38 + ssidLength); i++) {
                ssidByte[i - (rtap + 38)] = _rawData[i];
            }

            return new String(ssidByte);
        } else
            return "";

    }

    public String getBSSIDfromBeacon() {
        int rtap = getRtapLength();
        if (_rawData.length >= (rtap + 22)) {
            byte[] bssidByte = new byte[6];
            for (int i = rtap + 16; i <= (rtap + 21); i++) {
                bssidByte[i - (rtap + 16)] = _rawData[i];
            }

            String bssidString = DataConvert.bytesToMac(bssidByte);
//            bssidString = bssidString.trim();
//            bssidString = bssidString.replaceAll(" ", ":");
            return bssidString;
        } else
            return "";
    }

    public int getRtapLength() {
        int len = (_rawData[3] << 8) | (_rawData[2]);
        return len;
    }

    public Integer getsequenceCount() {
        return this.sequenceCount;
    }

    public Integer getFullLength() {
        return this._headerLen + this._dataLen;
    }
    public long getFrameTimestamp(){
        byte[] first4bytes,sec4bytes;
        if (this._rawHeader != null && this._rawHeader.length == 16) {
            first4bytes = Arrays.copyOfRange(this._rawHeader, 0, 4);//0~7;
            sec4bytes = Arrays.copyOfRange(this._rawHeader, 4, 8);//0~7;
            long ts = ByteBuffer.wrap(first4bytes)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getInt();
            long tus = ByteBuffer.wrap(sec4bytes)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getInt();
            this.timestamp = (ts<<32)+tus;
            return this.timestamp;
        } else {
            return 0;
        }
    }


    public PacketInfo buildQosPacketInfo() {
        this._raw_radioTap = Arrays.copyOfRange(this._rawData, 0, 30);

        if (this.isValidQosPacket()) {
            PacketInfo info = new PacketInfo();

            info.receivedTime = getFrameTimestamp();
            info.length = getPacketLength();
            info.channel = getPacketChannel();
            info.signalStrength = getPacketSignalStength();
            info.count = this.sequenceCount;
            info.durationT = getPacketDuration();
            info.flags = getPacketFlag();
            info.type = getPacketType();
            info.macDes = getPacketDes();
            info.macTrans = getPacketTrans();
            info.macSrc = getPacketSrc();
            //info.macAll=getPacketAllMac();
            return info;

        } else {
            //Logg.w(TAG,"Check validation before use BuildingMethods");
            return null;
        }

    }


    public int getPacketLength() {
        byte[] lastfour;
        if (this._rawHeader != null && this._rawHeader.length == 16) {
            lastfour = Arrays.copyOfRange(this._rawHeader, 12, 16);//12~15;
            this.fullLength = ByteBuffer.wrap(lastfour)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .getInt();
        } else {
            return 0;
        }

        return this.fullLength;
    }

    public int getPacketChannel() {
        byte[] channelByte2 = new byte[]{0, 0, 0, 0};
        channelByte2[0] = this._raw_radioTap[0x12];
        channelByte2[1] = this._raw_radioTap[0x13];

        int channelNum = ByteBuffer.wrap(channelByte2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        return channelNum;
    }

    public byte getPacketSignalStength() {
        byte signalByte = this._raw_radioTap[0x16];
        //return (char)signalByte;
        return signalByte;
    }

    public byte getPacketType() {
        byte typeP = this._rawData[30];

        return typeP;

    }

    public byte getPacketFlag() {
        byte flag = this._rawData[31];

        return flag;
    }

    public int getPacketDuration() {
        //scale: millisecond
        byte[] dur = new byte[]{0, 0, 0, 0};
        dur[0] = this._rawData[32];
        dur[1] = this._rawData[33];
        int durTime = ByteBuffer.wrap(dur)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt();
        return durTime;
    }

    public boolean isValidQosPacket() {
        if (this._rawData == null
                || this._rawHeader == null
                || this._dataLen <= VALID_QOS_DATA_MIN_LEN) {
            return false;
        }
        if (getPacketType() != flagOfQosFrame ||
                (getPacketFlag() & maskForflagOfQosFrameDsbits) != flagOfQosFrameDsbits
                ) {//subtype and  flag check;
            return false;
        }

        return true;
    }


    public String getPacketDes() {
        byte[] d = Arrays.copyOfRange(this._rawData, 34, 40);

        if (Arrays.equals(bffff0x4, d)) {
            return Packet.ffff0x4;//fast return an existed object,rather than build ffff... again.
        } else {
            return DataConvert.bytesToMac(d);
        }
    }


    public String getPacketTrans() {
        byte[] t = Arrays.copyOfRange(this._rawData, 46, 52);
        if (Arrays.equals(bffff0x4, t)) {
            return Packet.ffff0x4;
        } else {
            return DataConvert.bytesToMac(t);
        }
    }

    public String getPacketSrc() {
        byte[] s = Arrays.copyOfRange(this._rawData, 40, 46);
        if (Arrays.equals(bffff0x4, s)) {
            return Packet.ffff0x4;
        } else {
            return DataConvert.bytesToMac(s);
        }

    }

    public String getPacketAllMac() {
        if (!isValidQosPacket()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getPacketSrc()).append('|')
                .append(getPacketTrans()).append('|')
                .append(getPacketDes());
        return stringBuilder.toString();
    }


}
//-----------------------------------------------------------------------------
