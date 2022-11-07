// ICalculateInterface.aidl
package com.realsil.test.server;

import com.realsil.test.server.Person;
import com.realsil.test.server.IOnNewPersonArrivedListener;

// Declare any non-default types here with import statements

interface ICalculateInterface {
    int addNum(int num1, int num2);
    List<Person> addPerson(in Person person);

    void registerListener(IOnNewPersonArrivedListener listener);
    void unregisterListener(IOnNewPersonArrivedListener listener);
}