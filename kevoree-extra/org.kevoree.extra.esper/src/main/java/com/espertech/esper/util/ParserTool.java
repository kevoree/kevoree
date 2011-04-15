package com.espertech.esper.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tools to transform the ANTLR-generated parser (EsperEPL2GrammarParser.java) method "specialStateTransition"
 * to move case-checking to separate methods. This is required as ANTLR 3.2 generates
 * a method that results in more then 64k bytecode. Through the simple refactoring that moves each case-check
 * to a separate method the bytecode size gets much smaller then 64k for the method. Also see file "etc/antlrtool.(sh, cmd)". 
 */
public class ParserTool {

    public static void main(String[] args) {

        if (args.length == 0) {
            throw new RuntimeException("Required filename parameter is not provided");
        }
        String filename = args[0];
        List<String> lines = readFile(filename);
        System.out.println("Read " + lines.size() + " lines read from file " + filename);

        List<String> result = new ArrayList<String>();
        for (String line : lines) {
            int indexComment = line.indexOf("// ");
            if (indexComment != -1) {
                line = line.substring(0, indexComment);
            }
            if (line.trim().length() == 0) {
                continue;
            }

            result.add(line);
        }

        transformSpecialStateTransition(result);

        writeFile(filename, result);
        System.out.println("Wrote " + result.size() + " lines to file " + filename);
    }

    public static String transform(String text) {
        List<String> lines = textToLines(text);
        transformSpecialStateTransition(lines);
        return linesToText(lines);
    }

    private static void transformSpecialStateTransition(List<String> inputbuf) {
        int startLineNum = findLineNum(inputbuf, "public int specialStateTransition", 0);
        if (startLineNum == -1) {
            return;
        }

        int endLineNum = findMethodEndLine(inputbuf, startLineNum);
        if (endLineNum == -1) {
            return;
        }

        int firstCaseLineNum = findLineNum(inputbuf, "case 0", startLineNum);
        int caseEndLineNum = findLineNum(inputbuf, "if (state.backtracking>0)", startLineNum);
        List<String> headerLines = getLinesBetween(inputbuf, startLineNum, firstCaseLineNum);
        List<String> trailerLines = getLinesBetween(inputbuf, caseEndLineNum, endLineNum + 1);

        List<List<String>> caseHandlers = new ArrayList<List<String>>();
        List<List<String>> methods = new ArrayList<List<String>>();
        int caseNum = 0;
        while(true) {
            int caseLineNum = findLineNum(inputbuf, "case " + caseNum + " :", startLineNum);
            if (caseLineNum == -1) {
                break;
            }
            int breakLineNum = findLineNum(inputbuf, "break", caseLineNum);

            List<String> method = new ArrayList<String>();
            method.add("  private int sst_" + caseNum + "() {");
            method.add("    int s = -1;");
            method.addAll(getLinesBetween(inputbuf, caseLineNum+1, breakLineNum - 1));
            method.add("    return s;");
            method.add("  }");
            methods.add(method);

            List<String> caseHandler = new ArrayList<String>();
            caseHandler.add("  case " + caseNum + ": ");
            caseHandler.add("    s = " + "sst_" + caseNum + "();");
            caseHandler.add("    if ( s>=0 ) return s;");
            caseHandler.add("    break;");
            caseHandlers.add(caseHandler);

            caseNum++;
        }

        List<String> replacementMethod = new ArrayList<String>();
        replacementMethod.addAll(headerLines);  // add header
        for (List<String> caseHandler : caseHandlers) {
            replacementMethod.addAll(caseHandler);
        }
        replacementMethod.add("  }"); // add trailer
        replacementMethod.addAll(trailerLines);
        for (List<String> method : methods) {   // add methods
            replacementMethod.addAll(method);
        }

        replaceLines(inputbuf, startLineNum, endLineNum + 1, replacementMethod);
    }

    private static void replaceLines(List<String> input, int start, int end, List<String> replacement) {
        int count = end - start;
        for (int i = 0; i < count; i++) {
            input.remove(start);
        }

        for (int i = 0; i < replacement.size(); i++) {
            input.add(start + i, replacement.get(i));
        }
    }

    private static List<String> getLinesBetween(List<String> input, int start, int end) {
        List<String> result = new ArrayList<String>();
        for (int i = start; i < end; i++) {
            result.add(input.get(i));
        }
        return result;
    }

    private static int findMethodEndLine(List<String> lines, int lineNum) {
        int open = 1;
        int current = lineNum + 1;
        while(current < lines.size()) {
            String line = lines.get(current);
            for (int c = 0; c < line.length(); c++) {
                if (line.charAt(c) == '{') {
                    open++;
                }
                if (line.charAt(c) == '}') {
                    open--;
                    if (open == 0) {
                        return current;
                    }
                }
            }
            current++;
        }
        return -1;
    }

    private static int findLineNum(List<String> lines, String name, int startIndex) {
        for (int i = startIndex; i < lines.size(); i++) {
            if (lines.get(i).trim().startsWith(name)) {
                return i;
            }
        }
        return -1;
    }

    private static void readFile(BufferedReader reader, List<String> list) throws IOException
    {
        String text;
        // repeat until all lines is read
        while ((text = reader.readLine()) != null)
        {
            list.add(text);
        }
    }

    public static List<String> readFile(InputStream is)
    {
        InputStreamReader isr = new InputStreamReader(is);
        try {
            return readFile(isr);
        }
        finally {
            try {
                isr.close();
            }
            catch (IOException e) {
                // fine
            }
        }
    }

    public static List<String> readFile(Reader reader)
    {
        List<String> list = new ArrayList<String>();
        BufferedReader bufferedReader = null;
        try
        {
            bufferedReader = new BufferedReader(reader);
            readFile(bufferedReader, list);
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException("File not found: " + e.getMessage(), e);
        }
        catch (IOException e)
        {
            throw new RuntimeException("IO Error reading file: " + e.getMessage(), e);
        }
        finally
        {
            try
            {
                if (bufferedReader != null)
                {
                    bufferedReader.close();
                }
            }
            catch (IOException e)
            {
            }
        }

        return list;
    }

    public static List<String> readFile(String file)
    {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            return readFile(fileReader);
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException("File not found: " + e.getMessage(), e);
        }
        finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                }
                catch (IOException e) {
                    // fine
                }
            }
        }
    }

    public static String linesToText(List<String> lines) {
        StringWriter writer = new StringWriter();
        for (String line : lines) {
            writer.append(line)
                    .append(System.getProperty(
                            "line.separator"));
        }
        return writer.toString();
    }

    public static List<String> textToLines(String input)
    {
        BufferedReader reader = null;
        List<String> list = new ArrayList<String>();
        try
        {
            reader = new BufferedReader(new StringReader(input));
            String text = null;

            // repeat until all lines is read
            while ((text = reader.readLine()) != null)
            {
                list.add(text);
            }
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException("File not found: " + e.getMessage(), e);
        }
        catch (IOException e)
        {
            throw new RuntimeException("IO Error reading file: " + e.getMessage(), e);
        }
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            }
            catch (IOException e)
            {
            }
        }

        return list;
    }

    public static void writeFile(String file, List<String> lines) {
        FileWriter writer = null;

        try {
            writer = new FileWriter(file, false);            
            for (String line : lines) {
                writer.append(line)
                        .append(System.getProperty(
                                "line.separator"));
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("File write problem: " + ex.getMessage(), ex);
        }
        finally {
            if (writer != null) {
                try
                {
                    writer.close();
                }
                catch (IOException e)
                {
                }                
            }
        }
    }

}
