package com.example.mentoringapp;

import java.util.List;

class OCRResponse {
    String language;
    Double textAngle;
    String orientation;
    List<Region> regions;

    class Region{
        public String boundingBox;
        public List<Line> lines;
    }

    class Line {
        public String boundingBox;
        public List<Word> words;
    }

    class Word {
        public String boundingBox;
        public String text;
    }

}
