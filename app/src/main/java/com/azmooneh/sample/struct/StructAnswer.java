package com.azmooneh.sample.struct;

import android.os.Parcel;
import android.os.Parcelable;

public class StructAnswer implements Parcelable {
    public boolean state;
    public String  text;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.state ? (byte) 1 : (byte) 0);
        dest.writeString(this.text);
    }

    public StructAnswer() {
    }

    protected StructAnswer(Parcel in) {
        this.state = in.readByte() != 0;
        this.text = in.readString();
    }

    public static final Parcelable.Creator<StructAnswer> CREATOR = new Parcelable.Creator<StructAnswer>() {
        @Override
        public StructAnswer createFromParcel(Parcel source) {
            return new StructAnswer(source);
        }

        @Override
        public StructAnswer[] newArray(int size) {
            return new StructAnswer[size];
        }
    };
}
