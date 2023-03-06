/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/12/7 19:25</create-date>
 *
 * <copyright file="DemoChineseNameRecoginiton.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014+ 上海林原信息科技有限公司. All Right Reserved+ http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.demo;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

import java.io.*;
import java.util.List;

/**
 * 人名、地名识别
 * @author hankcs
 */

public class My {
    public static void main(String[] args) throws IOException {
        int cnt = 1;
        while (true) {
            if (!new File("D:\\数据科学大作业\\案件文本\\案件文本"+cnt+".txt").exists()) {
                continue;
            }
            String pathname  = "D:\\数据科学大作业\\案件文本\\案件文本"+cnt+".txt";
            cnt++;

            String testCase = "";
            try{
                FileReader reader = new FileReader(pathname);
                BufferedReader br = new BufferedReader(reader);
                String line;
                while ((line = br.readLine()) != null) {
                    // 一次读入一行数据
                    testCase = testCase + line;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            Segment nShortSegment = new NShortSegment().enableNameRecognize(true).enablePlaceRecognize(true).enableOrganizationRecognize(true).enableTranslatedNameRecognize(true);
            List<Term> termList = nShortSegment.seg(testCase.replace(" ", "").replace("　", ""));
            //System.out.println(termList);

            //部分特例分析
            if (testCase.contains("被告人") && testCase.contains("，") && testCase.indexOf("被告人") <= testCase.indexOf("，")){
                String name = "";
                String str = testCase.substring(testCase.indexOf("被告人"), testCase.indexOf("，"));
                name = str.substring(3);
                termList.add(0, new Term(name, Nature.nr));
            }

            //合并
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

            //去重
            for (int i = termList.size()-1; i >= 0; i--){
                for (int j = 0; j < i; j++){
                    if (termList.get(i).equals(termList.get(j))){
                        termList.remove(i);
                        break;
                    }
                }
            }
            //System.out.println(termList);

            //初步筛选
            for (int i = termList.size()-1; i >= 0; i--){
                String tag = termList.get(i).toString().substring(termList.get(i).toString().indexOf("/")+1);
                if (!(tag.startsWith("v") || tag.startsWith("a") || tag.startsWith("b") ||
                    tag.startsWith("nz") || tag.startsWith("ns") || tag.startsWith("nt") || tag.startsWith("nr"))){
                    termList.remove(i);
                }
            }
            //System.out.println(termList);

            //特例筛选
            for (int i = termList.size()-1; i >= 0; i--){
                String word = termList.get(i).toString().substring(0, termList.get(i).toString().indexOf("/"));
                String tag = termList.get(i).toString().substring(termList.get(i).toString().indexOf("/")+1);
                if (tag.startsWith("vshi") || tag.startsWith("vyou") ||
                    (tag.startsWith("b") && !(word.equals("男") || word.equals("女"))) ||
                    (tag.startsWith("nt") && !(word.endsWith("院")))){
                    termList.remove(i);
                }
            }
            //System.out.println(termList);

            //优先级排序
            //当事人 nr
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

            //性别 b
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

            //民族 nz
            int num3 = 0;
            for (int i = num1 + num2; i < termList.size(); i++){
                if (termList.get(i).nature.startsWith("nz") && termList.get(i).word.endsWith("族")){
                    Term temp = termList.get(i);
                    termList.set(i, termList.get(num1+num2+num3));
                    termList.set(num1+num2+num3, temp);
                    num3++;
                }
            }
            //System.out.println(termList);

            //出生地 ns
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

            //相关法院 nt/nto
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

            //案由 nz/v/a
            //动词与形容词分类
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


            //TODO 保存标注文件到本地json
            //     只需修改几个文件的目录
            BufferedWriter writer = null;
            //当事人.json
            File file1 = new File("D:\\数据科学大作业\\标注\\当事人.json");
            if(file1.exists()) {
                file1.delete();
                file1.createNewFile();
            } // 创建新文件,有同名的文件的话直接覆盖
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file1,false), "UTF-8"));
                writer.write("[\n");
                for (int i = 0; i < num1; i++){
                    writer.write("\""+termList.get(i).toString()+"\"");
                    if (i != num1-1) writer.write(",");
                }
                writer.write("\n]");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(writer != null){
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //性别.json
            File file2 = new File("D:\\数据科学大作业\\标注\\性别.json");
            if(file2.exists()) {
                file2.delete();
                file2.createNewFile();
            } // 创建新文件,有同名的文件的话直接覆盖
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2,false), "UTF-8"));
                writer.write("[\n");
                for (int i = num1; i < num1+num2; i++){
                    writer.write("\""+termList.get(i).toString()+"\"");
                    if (i != num1+num2-1) writer.write(",");
                }
                writer.write("\n]");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(writer != null){
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //民族.json
            File file3 = new File("D:\\数据科学大作业\\标注\\民族.json");
            if(file3.exists()) {
                file3.delete();
                file3.createNewFile();
            } // 创建新文件,有同名的文件的话直接覆盖
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file3,false), "UTF-8"));
                writer.write("[\n");
                for (int i = num1+num2; i < num1+num2+num3; i++){
                    writer.write("\""+termList.get(i).toString()+"\"");
                    if (i != num1+num2+num3-1) writer.write(",");
                }
                writer.write("\n]");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(writer != null){
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //出生地.json
            File file4 = new File("D:\\数据科学大作业\\标注\\出生地.json");
            if(file4.exists()) {
                file4.delete();
                file4.createNewFile();
            } // 创建新文件,有同名的文件的话直接覆盖
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file4,false), "UTF-8"));
                writer.write("[\n");
                for (int i = num1+num2+num3; i < num1+num2+num3+num4; i++){
                    writer.write("\""+termList.get(i).toString()+"\"");
                    if (i != num1+num2+num3+num4-1) writer.write(",");
                }
                writer.write("\n]");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(writer != null){
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //案由(名词).json
            File file5 = new File("D:\\数据科学大作业\\标注\\案由_名词.json");
            if(file5.exists()) {
                file5.delete();
                file5.createNewFile();
            } // 创建新文件,有同名的文件的话直接覆盖
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file5,false), "UTF-8"));
                writer.write("[\n");
                for (int i = num1+num2+num3+num4; i < num1+num2+num3+num4+num5_1; i++){
                    writer.write("\""+termList.get(i).toString()+"\"");
                    if (i != num1+num2+num3+num4+num5_1-1) writer.write(",");
                }
                writer.write("\n]");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(writer != null){
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //案由(动词).json
            File file6 = new File("D:\\数据科学大作业\\标注\\案由_动词.json");
            if(file6.exists()) {
                file6.delete();
                file6.createNewFile();
            } // 创建新文件,有同名的文件的话直接覆盖
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file6,false), "UTF-8"));
                writer.write("[\n");
                for (int i = num1+num2+num3+num4+num5_1; i < num1+num2+num3+num4+num5_1+num5_2; i++){
                    writer.write("\""+termList.get(i).toString()+"\"");
                    if (i != num1+num2+num3+num4+num5_1+num5_2-1) writer.write(",");
                }
                writer.write("\n]");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(writer != null){
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //案由(形容词).json
            File file7 = new File("D:\\数据科学大作业\\标注\\案由_形容词.json");
            if(file7.exists()) {
                file7.delete();
                file7.createNewFile();
            } // 创建新文件,有同名的文件的话直接覆盖
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file7,false), "UTF-8"));
                writer.write("[\n");
                for (int i = num1+num2+num3+num4+num5_1+num5_2; i < num1+num2+num3+num4+num5; i++){
                    writer.write("\""+termList.get(i).toString()+"\"");
                    if (i != num1+num2+num3+num4+num5-1) writer.write(",");
                }
                writer.write("\n]");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(writer != null){
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //相关法院.json
            File file8 = new File("D:\\数据科学大作业\\标注\\相关法院.json");
            if(file8.exists()) {
                file8.delete();
                file8.createNewFile();
            } // 创建新文件,有同名的文件的话直接覆盖
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file8,false), "UTF-8"));
                writer.write("[\n");
                for (int i = num1+num2+num3+num4+num5; i < termList.size(); i++){
                    writer.write("\""+termList.get(i).toString()+"\"");
                    if (i != termList.size()-1) writer.write(",");
                }
                writer.write("\n]");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(writer != null){
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //TODO 读取本地txt文件
        //     只需修改该目录

    }
}
