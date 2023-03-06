package com.hankcs.demo;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

import java.io.*;
import java.util.List;

import java.applet.Applet;
/**
 * ?????????????
 * @author hankcs
 */

public class My1{

    public static void main(String[] args) throws IOException, InterruptedException {
        Thread.sleep(10000);
        //TODO ???????txt???
        //     ?????????
        String pathname  = "D:\\???????????\\???????.txt";

        String testCase = "";
        try{
            FileReader reader = new FileReader(pathname);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                // ??¦Æ??????????
                testCase = testCase + line;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        Segment nShortSegment = new NShortSegment().enableNameRecognize(true).enablePlaceRecognize(true).enableOrganizationRecognize(true).enableTranslatedNameRecognize(true);
        List<Term> termList = nShortSegment.seg(testCase.replace(" ", "").replace("??", ""));
        //System.out.println(termList);

        //????????????
        if (testCase.contains("??????") && testCase.contains("??") && testCase.indexOf("??????") <= testCase.indexOf("??")){
            String name = "";
            String str = testCase.substring(testCase.indexOf("??????"), testCase.indexOf("??"));
            name = str.substring(3);
            termList.add(0, new Term(name, Nature.nr));
        }

        //???
        for (int i = termList.size()-1; i >= 1; i--){
            String word2 = termList.get(i).toString().substring(0, termList.get(i).toString().indexOf("/"));
            String tag2 = termList.get(i).toString().substring(termList.get(i).toString().indexOf("/")+1);
            String word1 = termList.get(i-1).toString().substring(0, termList.get(i-1).toString().indexOf("/"));
            String tag1 = termList.get(i-1).toString().substring(termList.get(i-1).toString().indexOf("/")+1);
            if ((tag1.startsWith("ns") && tag2.startsWith("ns")) || (tag1.startsWith("ns") && tag2.startsWith("nt"))){
                String word = word1 + word2;
                termList.set(i-1, new Term(word, termList.get(i).nature));
                termList.remove(i);
            }
        }
        //System.out.println(termList);

        //???
        for (int i = termList.size()-1; i >= 0; i--){
            for (int j = 0; j < i; j++){
                if (termList.get(i).equals(termList.get(j))){
                    termList.remove(i);
                    break;
                }
            }
        }
        //System.out.println(termList);

        //??????
        for (int i = termList.size()-1; i >= 0; i--){
            String tag = termList.get(i).toString().substring(termList.get(i).toString().indexOf("/")+1);
            if (!(tag.startsWith("v") || tag.startsWith("a") || tag.startsWith("b") ||
                tag.startsWith("nz") || tag.startsWith("ns") || tag.startsWith("nt") || tag.startsWith("nr"))){
                termList.remove(i);
            }
        }
        //System.out.println(termList);

        //??????
        for (int i = termList.size()-1; i >= 0; i--){
            String word = termList.get(i).toString().substring(0, termList.get(i).toString().indexOf("/"));
            String tag = termList.get(i).toString().substring(termList.get(i).toString().indexOf("/")+1);
            if (tag.startsWith("vshi") || tag.startsWith("vyou") ||
                (tag.startsWith("b") && !(word.equals("??") || word.equals("?"))) ||
                (tag.startsWith("nt") && !(word.endsWith("?")))){
                termList.remove(i);
            }
        }
        //System.out.println(termList);

        //?????????
        //?????? nr
        int num1 = 0;
        for (int i = 0; i < termList.size(); i++){
            if (termList.get(i).nature.startsWith("nr")){
                num1++;
            }
        }
        for (int i = termList.size()-1; i >= 0; i--){
            boolean flag = true;
            for (int j = 0; j < num1; j++){
                if (!termList.get(j).nature.startsWith("nr")){
                    flag = false;
                }
            }
            if (flag) break;

            if (termList.get(i).nature.startsWith("nr")){
                Term temp = termList.get(i);
                for (int j = 0; j < termList.size(); j++){
                    if (!termList.get(j).nature.startsWith("nr")){
                        termList.set(i, termList.get(j));
                        termList.set(j, temp);
                        break;
                    }
                }
            }
        }
        //System.out.println(termList);

        //??? b
        int num2 = 0;
        for (int i = num1; i < termList.size(); i++){
            if (termList.get(i).nature.startsWith("b")){
                Term temp = termList.get(i);
                termList.set(i, termList.get(num1+num2));
                termList.set(num1+num2, temp);
                num2++;
                break;
            }
        }
        //System.out.println(termList);

        //???? nz
        int num3 = 0;
        for (int i = num1 + num2; i < termList.size(); i++){
            if (termList.get(i).nature.startsWith("nz") && termList.get(i).word.endsWith("??")){
                Term temp = termList.get(i);
                termList.set(i, termList.get(num1+num2+num3));
                termList.set(num1+num2+num3, temp);
                num3++;
            }
        }
        //System.out.println(termList);

        //?????? ns
        int num4 = 0;
        for (int i = num1 + num2 + num3; i < termList.size(); i++){
            if (termList.get(i).nature.startsWith("ns")){
                Term temp = termList.get(i);
                termList.set(i, termList.get(num1+num2+num3+num4));
                termList.set(num1+num2+num3+num4, temp);
                num4++;
            }
        }
        //System.out.println(termList);

        //????? nt/nto
        int num6 = 0;
        for (int i = termList.size()-1; i >= num1 + num2 + num3 + num4; i--){
            if (termList.get(i).nature.startsWith("nt")){
                Term temp = termList.get(i);
                termList.set(i, termList.get(termList.size()-1-num6));
                termList.set(termList.size()-1-num6, temp);
                num6++;
            }
        }
        //System.out.println(termList);

        //???? nz/v/a
        //??????????????
        int num5_1 = 0;
        for (int i = num1 + num2 + num3 + num4; i < termList.size(); i++){
            if (termList.get(i).nature.startsWith("nz")){
                Term temp = termList.get(i);
                termList.set(i, termList.get(num1+num2+num3+num4+num5_1));
                termList.set(num1+num2+num3+num4+num5_1, temp);
                num5_1++;
            }
        }

        int num5_2 = 0;
        for (int i = num1 + num2 + num3 + num4 + num5_1; i < termList.size(); i++){
            if (termList.get(i).nature.startsWith("v")){
                Term temp = termList.get(i);
                termList.set(i, termList.get(num1+num2+num3+num4+num5_1+num5_2));
                termList.set(num1+num2+num3+num4+num5_1+num5_2, temp);
                num5_2++;
            }
        }

        int num5 = termList.size() - num1 - num2 - num3 - num4 - num6;
        int num5_3 = num5 - num5_1 - num5_2;
        //System.out.println(termList);


        //TODO ???????????????json
        //     ?????????
        File outFile = new File("D:\\???????????\\???.json");

        if(outFile.exists()) {
            outFile.delete();
            outFile.createNewFile();
        } // ?????????,???????????????????

        Writer out;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile,true), "utf-8"), 10240);
            for (int i = 0; i < termList.size(); i++) {
                //???????
                if (i == 0) out.write("??????:\n");
                else if (i == num1) out.write("???:\n");
                else if (i == num1+num2) out.write("????:\n");
                else if (i == num1+num2+num3) out.write("??????:\n");
                else if (i == num1+num2+num3+num4) out.write("????(????):\n");
                else if (i == num1+num2+num3+num4+num5_1) out.write("????(????):\n");
                else if (i == num1+num2+num3+num4+num5_1+num5_2) out.write("????(?????):\n");
                else if (i == num1+num2+num3+num4+num5) out.write("?????:\n");

                out.write(String.valueOf(termList.get(i)));

                if (i == num1-1 || i == num1+num2-1  || i == num1+num2+num3-1 || i == num1+num2+num3+num4-1
                    || i == num1+num2+num3+num4+num5_1-1 || i == num1+num2+num3+num4+num5_1+num5_2-1
                    || i == num1+num2+num3+num4+num5_1+num5_2+num5_3-1 || i == num1+num2+num3+num4+num5+num6-1){
                    out.write("\r\n");
                }else {
                    out.write(" ");
                } //?????????
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
