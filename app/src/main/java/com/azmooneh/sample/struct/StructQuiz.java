package com.azmooneh.sample.struct;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class StructQuiz implements Parcelable {
    public int                     id;
    public String                  quiz;
    public ArrayList<StructAnswer> answers;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.quiz);
        dest.writeTypedList(this.answers);
    }

    public StructQuiz() {
    }

    protected StructQuiz(Parcel in) {
        this.id = in.readInt();
        this.quiz = in.readString();
        this.answers = in.createTypedArrayList(StructAnswer.CREATOR);
    }

    public static final Parcelable.Creator<StructQuiz> CREATOR = new Parcelable.Creator<StructQuiz>() {
        @Override
        public StructQuiz createFromParcel(Parcel source) {
            return new StructQuiz(source);
        }

        @Override
        public StructQuiz[] newArray(int size) {
            return new StructQuiz[size];
        }
    };
}
