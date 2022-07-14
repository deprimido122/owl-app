package com.example.mentoringapp;

import java.util.ArrayList;

class TranslateResponse{

   DetectedLanguage detectedLanguage;
   ArrayList<Translation> translations;

   class DetectedLanguage{
      public String language;
      public float score;
   }

   class Translation {
      public String text;
      public String to;
   }
}