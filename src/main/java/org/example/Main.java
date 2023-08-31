package org.example;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static String BASE_PATH = "";

    private static final String SUBTITLES_SUFFIX = ".ass";
    private static final String MKV_SUFFIX = ".mkv";
    private static final String MP4_SUFFIX = ".mp4";

    public static void main(String[] args) {
        //open java file chooser
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("choose directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //set base path
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            BASE_PATH = fileChooser.getSelectedFile() + "/";
        }
        //find subtitles files
        List<String> originalSubtitlesFileNames = readFileNameBySuffix(SUBTITLES_SUFFIX);
        if (originalSubtitlesFileNames.size() == 0) {
            System.out.println("Can not find subtitles");
            return;
        }
        //find mp4 or mkv files
        List<String> videoFileNames = readFileNameBySuffix(MKV_SUFFIX);
        if (videoFileNames.size() == 0) {
            videoFileNames = readFileNameBySuffix(MP4_SUFFIX);
        }
        if (videoFileNames.size() == 0) {
            System.out.println("Can not find mkv or mp4 videos");
            return;
        }
        //check the number of videos and subtitles is consistent
        if (originalSubtitlesFileNames.size() != videoFileNames.size()) {
            //get real video file names
            videoFileNames = getRealVideoFileNames(videoFileNames);
        }
        //get the same prefix
        String sameOriginalSubtitlesPrefix = getSamePrefix(originalSubtitlesFileNames.get(0), originalSubtitlesFileNames.get(1));
        String sameVideoPrefix = getSamePrefix(videoFileNames.get(0), videoFileNames.get(1));
        //Based on the same prefix,sort the file names.
        originalSubtitlesFileNames = sortFileNames(originalSubtitlesFileNames, sameOriginalSubtitlesPrefix);
        videoFileNames = sortFileNames(videoFileNames, sameVideoPrefix);
        //rename subtitles files by list index
        renameSubtitlesFileNames(originalSubtitlesFileNames, videoFileNames);
    }

    public static List<String> readFileNameBySuffix(final String suffix) {
        File folder = new File(BASE_PATH);
        //set filter
        FilenameFilter filter = (dir, name) -> name.endsWith(suffix);
        //get filenames
        String[] temp = folder.list(filter);
        //remove suffix
        List<String> result = new ArrayList<>();
        if (temp != null) {
            for (String s : temp) {
                //If u change subtitles format,remember change this number.
                result.add(s.substring(0, s.length() - 4));
            }
        }
        return result;
    }


    public static List<String> getRealVideoFileNames(List<String> fileNames) {
        HashMap<Integer, Integer> fileNameLengthMap = new HashMap<>();
        for (String fileName : fileNames) {
            if (fileName.contains("END")) {
                fileName = fileName.replace(" END", "");
            }
            if (fileNameLengthMap.containsKey(fileName.length())) {
                fileNameLengthMap.put(fileName.length(), fileNameLengthMap.get(fileName.length()) + 1);
            } else {
                fileNameLengthMap.put(fileName.length(), 0);
            }
        }
        int realFileNameLength = 0;
        int maxLengthCount = 0;
        for (Map.Entry<Integer, Integer> entry : fileNameLengthMap.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            if (value > maxLengthCount) {
                maxLengthCount = value;
                realFileNameLength = key;
            }
        }
        List<String> realFileNameList = new ArrayList<>();
        for (String fileName : fileNames) {
            if (fileName.contains(" END ")) {
                realFileNameList.add(fileName);
            }
            if (fileName.length() == realFileNameLength) {
                realFileNameList.add(fileName);
            }
        }
        return realFileNameList;
    }

    public static String getSamePrefix(String str1, String str2) {
        StringBuilder samePrefix = new StringBuilder();
        for (int i = 0; i < str1.length(); i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                break;
            } else {
                samePrefix.append(str1.charAt(i));
            }
        }
        String result = String.valueOf(samePrefix);
        if (result.charAt(result.length() - 1) != 32) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static List<String> sortFileNames(List<String> fileNames, String samePrefix) {
        List<String> result = new ArrayList<>(fileNames.size());
        for (int i = 0; i < fileNames.size(); i++) {
            result.add("");
        }
        int samePrefixCharAt = samePrefix.length();
        for (String fileName : fileNames) {
            int number = Integer.parseInt(fileName.substring(samePrefixCharAt, samePrefixCharAt + 2));
            result.set(number - 1, fileName);
        }
        return result;
    }

    public static void renameSubtitlesFileNames(List<String> originalSubtitlesFileNames, List<String> videoFileNames) {
        for (int i = 0; i < originalSubtitlesFileNames.size(); i++) {
            String oldName = BASE_PATH + originalSubtitlesFileNames.get(i) + SUBTITLES_SUFFIX;
            String newName = BASE_PATH + videoFileNames.get(i) + SUBTITLES_SUFFIX;
            File oldFile = new File(oldName);
            File newFile = new File(newName);
            boolean isRenamed = oldFile.renameTo(newFile);
            if (isRenamed) {
                System.out.println("Rename success: " + videoFileNames.get(i) + SUBTITLES_SUFFIX);
            } else {
                System.out.println("Rename failed : " + originalSubtitlesFileNames.get(i) + SUBTITLES_SUFFIX);
            }
        }
    }
}