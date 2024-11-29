package com.pesapal.felixvcs.core;

import java.util.Map;

public class Tree {
    private Map<String, String> files; // filePath -> blobHash

    public Tree() {}

    public Tree(Map<String, String> files) {
        this.files = files;
    }

    // Getters and Setters

    public Map<String, String> getFiles(){
        return files;
    }

    public void setFiles(Map<String, String> files){
        this.files = files;
    }
}
