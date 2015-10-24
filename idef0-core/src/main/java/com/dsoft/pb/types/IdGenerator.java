package com.dsoft.pb.types;

public interface IdGenerator {

    void addId(long i, long localReserved);

    long getNextId();

    void clear();

    void addId(long localId);

}
