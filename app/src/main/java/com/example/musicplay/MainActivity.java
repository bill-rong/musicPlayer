package com.example.musicplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

//    private File rootPath = Environment.getExternalStorageDirectory();
//    private File filePath = new File(rootPath,"文件夹名");
    private File filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    private ImageButton btnPlay;
    private ImageButton btnNext;
    private ImageButton btnPlayMode;
    private MusicService.MyBinder myBinder;             //
    private MyConnect myConnect;                       //
    private Timer timer;                                // 时间计时器
    private SeekBar skPro;                              // 进度条
    private TextView tvCurrTime;                        // 当前进度
    private TextView tvTotalTime;                       // 总时长
    private TextView tvMusicName;
    private ListView lvMusic;                           // 歌曲列表
    private MyBaseAdapter mAdapter;                     // 适配器
    private ArrayList<String []> musicList = null;        // 存放歌曲的名字
    //private ArrayList<String> musicTime = null;
    private int musicPosition = 0;                      // 存放当前音乐在ListView里的下标
    private int playModeIndex = 0;                           // 0顺序，1循环，2随机，3单曲
    private int[] playModeIcon = {R.drawable.list_order, R.drawable.list_cycle,
            R.drawable.random_play, R.drawable.single_cycle};       // 播放模式的图片


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initView();
        ActivityCompat.requestPermissions(this, new String[]
                {"android.permission.READ_EXTERNAL_STORAGE"}, 1);
        //File filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        String absolutePath = filePath.getAbsolutePath();
        musicList = Util.getMusicName(absolutePath, ".mp3");    // 获取music目录下的MP3

        mAdapter = new MyBaseAdapter();
        lvMusic.setAdapter(mAdapter);

        if (myConnect == null) {
            myConnect = new MyConnect();
        }
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, myConnect, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Permission permission = new Permission(this);
//        permission.checkPermission();      // Activity启动时检查权限
    }

    /**
     * 控件初始化
     */
    public void initView() {
        btnPlay = findViewById(R.id.btn_play);
        btnNext = findViewById(R.id.btn_next);
        btnPlayMode = findViewById(R.id.btn_play_mode);
        tvMusicName = findViewById(R.id.music_name);

        skPro = findViewById(R.id.sb_progress);
        tvCurrTime = findViewById(R.id.tv_play_time);
        tvTotalTime = findViewById(R.id.tv_total_time);

        lvMusic = findViewById(R.id.lv_music);
        lvMusic.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPlayMode.setOnClickListener(this);

        // btnPause.setOnClickListener(this);
        //lvMusic.setOnItemClickListener((AdapterView.OnItemClickListener) this);
        seekOnClick();
        listViewOnClick();
    }

    /**
     * 播放点击事件
     */
    @Override
    public void onClick(View view) {
        //Toast.makeText(this, file.toString(),Toast.LENGTH_SHORT).show();
        //File file = new File("data/data/com.example.musicplay", "water_hander.mp3");

        switch (view.getId()) {
            // 播放/暂停
            case R.id.btn_play:
                if(musicList.size() == 0){
                    Toast.makeText(this, "歌单没有歌曲", Toast.LENGTH_SHORT).show();
                    return;
                }
                File file = new File(filePath, musicList.get(musicPosition)[0]);
                String path = file.getAbsolutePath();
                if (file.exists() & file.length() > 0) {
                    if (!myBinder.getIsPlaying()) {
                        myBinder.play(path);
                        addTimer();
                        ((ImageButton) btnPlay).setBackgroundResource(R.drawable.pause);
                    } else {
                        myBinder.pause();
                        ((ImageButton) btnPlay).setBackgroundResource(R.drawable.play);
                    }
                } else {
                    Log.i("TAG", "文件不存在");
                }
                break;
             // 下一首
            case R.id.btn_next:
                if(musicList.size() == 0){
                    Toast.makeText(this, "歌单没有歌曲", Toast.LENGTH_SHORT).show();
                    return;
                }
                nextMusic();
                break;
            // 切换模式
            case R.id.btn_play_mode:
                playModeIndex = (playModeIndex + 1) % 4;
                String[] modeName = {"列表顺序播放", "列表循环播放", "随机播放", "单曲循环播放"};
                Toast.makeText(this, modeName[playModeIndex], Toast.LENGTH_SHORT).show();
                ((ImageButton) btnPlayMode).setBackgroundResource(playModeIcon[playModeIndex]);
                break;
        }
        if(musicList.size() > 0){
            String name = musicList.get(musicPosition)[0];
            tvMusicName.setText(name.substring(0, name.length() - 4));
        }
    }

    /**
     * 下一首
     */
    public void nextMusic(){
        int temp = Util.getNextMusicPosition(musicPosition, playModeIndex, musicList.size());
        if(temp == -1){
            Toast.makeText(this, "已经是最后一首了", Toast.LENGTH_SHORT).show();
            return;
        }
        musicPosition = temp;
        File nextPath = new File(filePath, musicList.get(musicPosition)[0]);
        //Toast.makeText(this,nextPath.toString(),Toast.LENGTH_SHORT).show();
        String nextAbsolutePath = nextPath.getAbsolutePath();
        myBinder.cut(nextAbsolutePath);
        if(timer == null)
            addTimer();
        ((ImageButton) btnPlay).setBackgroundResource(R.drawable.pause);
        mAdapter.notifyDataSetChanged();
        lvMusic.setSelection(musicPosition);
        String name = musicList.get(musicPosition)[0];
        tvMusicName.setText(name.substring(0, name.length() - 4));
    }

    /**
     * 创建服务
     */
    private class MyConnect implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("TAG", "onServiceConnected");
            myBinder = (MusicService.MyBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    /**
     * 添加时间计时器
     */
    private void addTimer() {
        if (timer == null) {
            timer = new Timer();        // 实例化时间计时器对象
            TimerTask task = new TimerTask() {      // 创建线程
                @Override
                public void run() {
                    int currTime = myBinder.getCurrTime();      // 获取当前进度，毫秒
                    int totalTime = myBinder.getTotalTime();    // 获取总进度，毫秒
                    Bundle bundle = new Bundle();       // 将数据封装到Bundle
                    bundle.putInt("currTime", currTime);
                    bundle.putInt("totalTime", totalTime);
                    Message message = handler.obtainMessage();
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            };
            timer.schedule(task, 5, 500);
        }

    }

    /**
     * 配置handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            int currTime = bundle.getInt("currTime");
            int totalTime = bundle.getInt("totalTime");
            skPro.setMax(totalTime);
            skPro.setProgress(currTime);
            tvCurrTime.setText(Util.format(currTime));
            tvTotalTime.setText(Util.format(totalTime));
            if(totalTime - currTime < 500 && myBinder.getIsPlaying() && currTime != 0 && totalTime != 0){
                nextMusic();
            }
        }
    };

    /**
     * 歌曲列表点击监听事件
     */
    public void listViewOnClick() {
        // lvMusic.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = new File(filePath, musicList.get(position)[0]);
                musicPosition = position;
                // Toast.makeText(MainActivity.this, file.toString(), Toast.LENGTH_SHORT).show();
                String path = file.getAbsolutePath();
                myBinder.cut(path);
                if(timer == null)
                    addTimer();
                ((ImageButton) btnPlay).setBackgroundResource(R.drawable.pause);
                lvMusic.setSelection(position);
                mAdapter.notifyDataSetChanged();
                String name = musicList.get(musicPosition)[0];
                tvMusicName.setText(name.substring(0, name.length() - 4));
                try {
                    //Toast.makeText(MainActivity.this, musicTime.get(position), Toast.LENGTH_SHORT).show();
                }catch (Exception e){

                }

            }
        });
    }

    /**
     * 进度条事件监听
     */
    private void seekOnClick() {
        skPro.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // 拖动过程中的事件
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (myBinder != null) {
                    tvCurrTime.setText(Util.format(seekBar.getProgress()));
                }

            }

            // 开始拖到时的事件
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //timer.purge();
            }

            // 结束拖动时的事件
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (myBinder != null && !myBinder.isNull()) {
                    Log.i("S", "1");
                    myBinder.seekToProgress(seekBar.getProgress());
                } else {

                }
                //addTimer();
            }
        });
    }

    /**
     * 自定义BaseAdapter适配器
     */
    class MyBaseAdapter extends BaseAdapter {
        // 获取歌曲数量
        @Override
        public int getCount() {
            return musicList.size();
        }

        // 获取歌曲名字
        @Override
        public Object getItem(int position) {
            return musicList.get(position);
        }

        // 获取歌曲对应的索引
        @Override
        public long getItemId(int position) {
            return position;
        }

        // 配置数据
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 初始化控件
            View view = View.inflate(MainActivity.this, R.layout.list_item, null);
            TextView mTextView = (TextView) view.findViewById(R.id.item_tv);
            TextView tvDurationTime = (TextView) view.findViewById(R.id.tv_time);
            ImageView imageView = (ImageView) view.findViewById(R.id.item_image);

            String mName = musicList.get(position)[0];
            mName = mName.substring(0, mName.length() - 4);
            mTextView.setText(mName);           // 列表中歌曲名字

            tvDurationTime.setText(musicList.get(position)[1]); // 列表中歌曲时长

            if (position == musicPosition) {        //  播放当前歌曲时，修改图标
                mTextView.setEnabled(false);
                imageView.setBackgroundResource(R.drawable.music_playing);
            } else {
                mTextView.setEnabled(true);
                imageView.setBackgroundResource(R.drawable.music);
            }

            return view;
        }

    }


}


