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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class IpAddress implements Parcelable {

    public String address;
    public int prefixLength;

    public IpAddress(String address, int prefixLength) {
        this.address = address;
        this.prefixLength = prefixLength;
    }

    @Override
    public String toString() {
        return address + "/" + prefixLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IpAddress)) {
            return false;
        }
        // Compare string value.
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, prefixLength);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.address);
        dest.writeInt(this.prefixLength);
    }

    private IpAddress(Parcel in) {
        this.address = in.readString();
        this.prefixLength = in.readInt();
    }

    public static final Creator<IpAddress> CREATOR = new Creator<IpAddress>() {
        @Override
        public IpAddress createFromParcel(Parcel source) {
            return new IpAddress(source);
        }

        @Override
        public IpAddress[] newArray(int size) {
            return new IpAddress[size];
        }
    };

}
