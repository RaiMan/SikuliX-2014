package com.sikulix.remoteserver.interfaces.entities;

/**
 * Author: Sergey Kuts
 */
public interface Image {

    String getPath();

    float getSimilarity();

    default String getValues() {
        return "[image path = " + getPath() +
                "; similarity = " + getSimilarity() + "]";
    }
}

