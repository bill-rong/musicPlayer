package com.example.musicplay;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MusicService extends Service {
    private MediaPlayer mediaPlayer;
    public MusicService() {
    }
    public class MyBinder extends Binder {
        private boolean isPlaying = false;
        public void play(String path){
            //Log.i("TAG", "MyBinderPlay");
            try{
                if(mediaPlayer == null){
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.start();
                            isPlaying = true;
                        }
                    });

                }else{
                    if (!mediaPlayer.isPlaying()){
                        mediaPlayer.start();
                        isPlaying = true;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        public void pause(){
            //Log.i("TAG", "MyBinderPause");
            if(mediaPlayer.isPlaying() && mediaPlayer != null){
                mediaPlayer.pause();
                isPlaying = false;
            }else if(mediaPlayer != null && (!mediaPlayer.isPlaying())){
                mediaPlayer.start();
                isPlaying = true;
            }
        }

        /**
         * 停止音乐的函数
         */
        private void stop() {
            if (mediaPlayer!=null) {
                mediaPlayer.pause();            //播放器暂停
                mediaPlayer.seekTo(0);    //调至0
                mediaPlayer.stop();             //停止
            }

        }

        /**
         * 切歌
         * @param path 下一首歌的路径
         */
        public void cut(String path) {
            if(mediaPlayer != null){
                stop();
//                重置多媒体播放器
                mediaPlayer.reset();
//                设置新的播放路径
                try {
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.start();
                            isPlaying = true;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else{
                this.play(path);
            }

        }

        public void onCompletion(){
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                }
            });
        }


        // 获取当前播放时间
        public int getCurrTime(){
            return mediaPlayer.getCurrentPosition();
        }

        // 获取音乐总时长
        public int getTotalTime(){ return mediaPlayer.getDuration();}

        // 修改音乐播放进度
        public void seekToProgress(int progress){mediaPlayer.seekTo(progress);}

        // 判断歌曲是否在播放
        public boolean getIsPlaying(){ return isPlaying; }

        // 判断mediaPlayer是否为空
        public boolean isNull(){ return mediaPlayer==null?true:false;}



    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("TAG", "IBinder");
        return new MyBinder();
    }
}
