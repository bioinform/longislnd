package com.bina.hdf5.interfaces;

/**
 * Created by bayo on 5/8/15.
 */
public interface EventGroupFactory {
    EventGroup getEventGroup(int index) throws Exception;
    int size();
}
