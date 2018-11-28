package edu.comp479.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Tokenizer {

    private ArrayList<String> tokens;

    public Tokenizer(){
        tokens = new ArrayList<>();
    }

    public List<String> getTokens(String text){
        StringTokenizer stringTokenizer = new StringTokenizer(text);

        while(stringTokenizer.hasMoreTokens()){
            this.tokens.add(stringTokenizer.nextToken());
        }

        return this.tokens;
    }

    public List<String> removeCap(List<String> tokens){

        List<String> temp = new ArrayList<>();

        for(String token: tokens){
            temp.add(token.toLowerCase());
        }

        return temp;
    }

    public List<String> removePunctuation(List<String> tokens){

        List<String> temp = new ArrayList<>();

        for(String token : tokens){

            temp.add(token.replaceAll("[\\W*]", " ").trim());
        }

        return tokens;

    }

    public List<String> removeDigits(List<String> tokens){
        List<String> temp = new ArrayList<>();

        for(String token : tokens){
            temp.add(token.trim().replaceAll("\\d*", ""));
        }

        return temp;
    }

}
