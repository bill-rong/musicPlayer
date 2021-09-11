package com.example.musicplay;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Util {
    /**
     * 时间换算
     * @param mill 时间（毫秒）
     * @return  返回换算结果
     */
    public static String format(int mill){
        int munite = mill / 1000 / 60;
        int second = mill / 1000 % 60;
        return String.format("%02d:%02d",munite, second);
    }

    /**
     * 扫描文件，获取fileAbsolutePath目录下type类型的文件
     * @param fileAbsolutePath 文件夹路径
     * @param type  文件类型
     * @return  返回扫描的文件列表
     */
    public static ArrayList<String[]> getMusicName(String fileAbsolutePath, String type){
        int mediaPlayerDuration = 0;

        ArrayList<String[]> musicNameList = new ArrayList<String[]>();
        File file = new File(fileAbsolutePath);
        File[] files = file.listFiles();        // 获取目录下的所有文件
        for(int i=0; i<files.length; i++){
            if(!files[i].isDirectory()){        // 判断是否为文件夹
                String fileName = files[i].getName();
                if(fileName.trim().toLowerCase().endsWith(type)){   // 判断文件类型是否为type
                    // 获取时长
                    try{
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(files[i].getAbsolutePath());
                        mediaPlayer.prepare();
                        mediaPlayerDuration = mediaPlayer.getDuration();
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                    }catch (Exception e){

                    }

                    String[] music = {fileName, Util.format(mediaPlayerDuration)};
                    musicNameList.add(music);
                }
            }
        }
        return musicNameList;
    }


    /**
     * 获取下一首歌的下标，-1表示列表播放到最后一首了
     * @param nowPosition   当前歌曲的下标
     * @param playMode      播放模式
     * @param musicListSize 歌单的歌曲数
     * @return
     */
    public static int getNextMusicPosition(int nowPosition, int playMode, int musicListSize){
        int nextPosition = -1;
        switch (playMode){
            case 0:
                if(nowPosition + 1 < musicListSize){
                    nextPosition = nowPosition + 1;
                }else {
                    nextPosition = -1;
                }
                break;
            case 1:
                nextPosition = (nowPosition + 1) % musicListSize;
                break;
            case 2:
                Random random = new Random();
                while((nextPosition = random.nextInt(musicListSize)) == nowPosition);
                break;
            case 3:
                nextPosition = nowPosition;
                break;
        }
        return nextPosition;
    }





}
