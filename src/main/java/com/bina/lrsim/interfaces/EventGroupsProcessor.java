package com.bina.lrsim.interfaces;

import java.io.IOException;

/**
 * Created by bayo on 7/15/15.
 */
public interface EventGroupsProcessor {
    void process(EventGroupFactory groups, int minLength, int flankMask) throws IOException;
}
