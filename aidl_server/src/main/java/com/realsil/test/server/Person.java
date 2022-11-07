package com.realsil.test.server;

import android.os.Parcel;
import android.os.Parcelable;

public class Person implements Parcelable{

    private String mName;
    private int mAge;


    protected Person(Parcel in) {
        mName = in.readString();
        mAge = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeInt(mAge);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getAge() {
        return mAge;
    }

    public void setAge(int age) {
        mAge = age;
    }

    public Person(String name, int age) {
        mName = name;
        mAge = age;
    }

    public Person() {

    }

    @Override
    public String toString() {
        return mName + "：" + mAge;
    }

}