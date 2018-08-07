package com.azmooneh.sample.struct;

import android.os.Parcel;
import android.os.Parcelable;

public class StructWord implements Parcelable {
    public int    id;
    public String word;
    public String mean;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.word);
        dest.writeString(this.mean);
    }

    public StructWord() {
    }

    protected StructWord(Parcel in) {
        this.id = in.readInt();
        this.word = in.readString();
        this.mean = in.readString();
    }

    public static final Parcelable.Creator<StructWord> CREATOR = new Parcelable.Creator<StructWord>() {
        @Override
        public StructWord createFromParcel(Parcel source) {
            return new StructWord(source);
        }

        @Override
        public StructWord[] newArray(int size) {
            return new StructWord[size];
        }
    };
}
