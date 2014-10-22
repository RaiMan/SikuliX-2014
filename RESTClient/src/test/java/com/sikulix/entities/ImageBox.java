package com.sikulix.entities;


import org.sikuli.remoteinterfaces.entities.Image;

/**
 * Author: Sergey Kuts
 */
public class ImageBox implements Image {

    private String path;
    private float similarity;

    public ImageBox(String path, float similarity) {
        this.path = path;
        this.similarity = similarity;
    }

    public String getPath() {
        return path;
    }

    public float getSimilarity() {
        return similarity;
    }

    public String toString() {
        return "[image path = " + getPath() +
                "; similarity = " + getSimilarity() + "]";
    }
}
