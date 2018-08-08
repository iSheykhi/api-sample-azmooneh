package com.azmooneh.sample.struct;

import android.os.Parcel;
import android.os.Parcelable;

public class StructStep implements Parcelable {
    public int stageId;
    public String title;
    public int state;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.stageId);
        dest.writeString(this.title);
        dest.writeInt(this.state);
    }

    public StructStep() {
    }

    protected StructStep(Parcel in) {
        this.stageId = in.readInt();
        this.title = in.readString();
        this.state = in.readInt();
    }

    public static final Parcelable.Creator<StructStep> CREATOR = new Parcelable.Creator<StructStep>() {
        @Override
        public StructStep createFromParcel(Parcel source) {
            return new StructStep(source);
        }

        @Override
        public StructStep[] newArray(int size) {
            return new StructStep[size];
        }
    };
}
