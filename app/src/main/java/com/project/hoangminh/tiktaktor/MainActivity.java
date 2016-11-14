package com.project.hoangminh.tiktaktor;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends Activity {

    public static final int START = 0;
    public static final int BOT1_COMPLETE = 1;
    public static final int BOT2_MAKEMOVE = 2;
    public static final int BOT2_COMPLETE = 3;
    public static final int BOT1_MAKEMOVE = 4;
    public static final int RESTART = 5;
    public static final int BOT1_RESTART_COMPLETE = 6;
    public static final int BOT2_RESTART_COMPLETE = 7;

    private TextView[] cells = new TextView[9];
    private TextView info;
    private Button button;
    private boolean isOver = false;
    private boolean isClicked = false;
    private int count = 0;
    private Handler uiThreadHanler;
    private Bot1Handler bot1Handler = new Bot1Handler();
    private Bot2Handler bot2Handler = new Bot2Handler();

    private String[] board = {
                                "", "", "",
                                "", "", "",
                                "", "", ""
                                            };
    private Thread bot1;
    private Thread bot2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiThreadHanler = new Handler() {
            public void handleMessage(Message m) {
                switch (m.what) {
                    case START:
                        Message msgToStart = bot1Handler.obtainMessage(START);
                        bot1Handler.sendMessage(msgToStart);
                        break;

                    case BOT1_COMPLETE:
                        writeBoard(m.arg1, "O");
                        updateCell(m.arg1, "O");
                        info.setText("Bot 1 played");
                        count++;
                        //code to check board
                        if(isWon()) {
                            isOver = true;
                            info.setText("Bot 1 won");
                            try {
                                bot1Handler.removeCallbacksAndMessages(null);
                                bot2Handler.removeCallbacksAndMessages(null);
                                uiThreadHanler.removeCallbacksAndMessages(null);
                            } catch (SecurityException e) {
                                System.out.println("Threads cannot be interrupted!");
                            }
                        } else {
                            if(count < 9) {
                                Message msgToBot2 = bot2Handler.obtainMessage(BOT2_MAKEMOVE);
                                bot2Handler.sendMessage(msgToBot2);
                            } else {
                                isOver = true;
                                info.setText("Draw");
                                try {
                                    bot1Handler.removeCallbacksAndMessages(null);
                                    bot2Handler.removeCallbacksAndMessages(null);
                                    uiThreadHanler.removeCallbacksAndMessages(null);
                                } catch (SecurityException e) {
                                    System.out.println("Threads cannot be interrupted!");
                                }
                            }
                        }
                        break;

                    case BOT2_COMPLETE:
                        writeBoard(m.arg1, "X");
                        updateCell(m.arg1, "X");
                        info.setText("Bot 2 played");
                        count++;
                            //code to check board
                        if (isWon()) {
                            isOver = true;
                            info.setText("Bot 2 won");
                            try {
                                bot1Handler.removeCallbacksAndMessages(null);
                                bot2Handler.removeCallbacksAndMessages(null);
                                uiThreadHanler.removeCallbacksAndMessages(null);
                            } catch (SecurityException e) {
                                System.out.println("Threads cannot be interrupted!");
                            }
                        } else {
                            if (count < 9) {
                                Message msgToBot1 = bot1Handler.obtainMessage(BOT1_MAKEMOVE);
                                bot1Handler.sendMessage(msgToBot1);
                            } else {
                                isOver = true;
                                info.setText("Draw");
                                try {
                                    bot1Handler.removeCallbacksAndMessages(null);
                                    bot2Handler.removeCallbacksAndMessages(null);
                                    uiThreadHanler.removeCallbacksAndMessages(null);
                                } catch (SecurityException e) {
                                    System.out.println("Threads cannot be interrupted!");
                                }
                            }
                        }
                        break;

                    case BOT1_RESTART_COMPLETE:
                        uiThreadHanler.removeCallbacksAndMessages(null);
                        Message msgBot2Restart = bot2Handler.obtainMessage(RESTART);
                        bot2Handler.sendMessageAtFrontOfQueue(msgBot2Restart);
                        break;

                    case BOT2_RESTART_COMPLETE:
                        uiThreadHanler.removeCallbacksAndMessages(null);
                        count = 0;
                        for(int i = 0; i < 9; i++) {
                            writeBoard(i, "");
                            updateCell(i, "");
                        }
                        info.setText("Battle started!");
                        Message msgBot1Move = bot1Handler.obtainMessage(BOT1_MAKEMOVE);
                        bot1Handler.sendMessage(msgBot1Move);
                }
            }
        };

        cells[0] = (TextView) findViewById(R.id.cell0);
        cells[1] = (TextView) findViewById(R.id.cell1);
        cells[2] = (TextView) findViewById(R.id.cell2);

        cells[3] = (TextView) findViewById(R.id.cell3);
        cells[4] = (TextView) findViewById(R.id.cell4);
        cells[5] = (TextView) findViewById(R.id.cell5);

        cells[6] = (TextView) findViewById(R.id.cell6);
        cells[7] = (TextView) findViewById(R.id.cell7);
        cells[8] = (TextView) findViewById(R.id.cell8);

        info = (TextView) findViewById(R.id.info);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isClicked) {
                    isClicked = true;
                    bot1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            bot1Handler = new Bot1Handler();
                            Looper.loop();
                        }
                    });
                    bot1.start();

                    bot2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Looper.prepare();
                            bot2Handler = new Bot2Handler();
                            Looper.loop();
                        }
                    });
                    bot2.start();

                    info.setText("Battle started!");
                    button.setText("RESTART");

                    Message msgToStart = uiThreadHanler.obtainMessage(START);
                    uiThreadHanler.sendMessage(msgToStart);

                } else {
                    if(!isOver) {
                        //restart while playing
                        uiThreadHanler.removeCallbacksAndMessages(null);
                        bot1Handler.removeCallbacksAndMessages(null);
                        bot2Handler.removeCallbacksAndMessages(null);

                        Message msgToRestart = bot1Handler.obtainMessage(RESTART);
                        bot1Handler.sendMessageAtFrontOfQueue(msgToRestart);
                    } else {
                        //restart after game finishes
                        isOver = false;
                        count = 0;
                        for(int i = 0; i < 9; i++) {
                            writeBoard(i, "");
                            updateCell(i, "");
                        }
                        uiThreadHanler.removeCallbacksAndMessages(null);
                        bot1Handler.removeCallbacksAndMessages(null);
                        bot2Handler.removeCallbacksAndMessages(null);

                        info.setText("Battle started!");
                        Message msgToRestart = uiThreadHanler.obtainMessage(START);
                        uiThreadHanler.sendMessage(msgToRestart);
                    }
                }
            }
        });
    }

    public class Bot1Handler extends Handler {
        public void handleMessage(Message m) {
            int position;
            Message msgToUI = uiThreadHanler.obtainMessage(BOT1_COMPLETE);
            switch (m.what) {
                case START:
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread interrupted!");
                    }
                    position = genRand();
                    msgToUI.arg1 = position;
                    uiThreadHanler.sendMessage(msgToUI);
                    break;

                case BOT1_MAKEMOVE:
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread interrupted!");
                    }
                    do {
                        position = genRand();
                    } while(!readBoard(position).equals(""));
                    msgToUI.arg1 = position;
                    uiThreadHanler.sendMessage(msgToUI);
                    break;

                case RESTART:
                    bot1Handler.removeCallbacksAndMessages(null);
                    Message msgBot1RestartComplete = uiThreadHanler.obtainMessage(BOT1_RESTART_COMPLETE);
                    uiThreadHanler.sendMessageAtFrontOfQueue(msgBot1RestartComplete);
                    break;
            }
        }
    }

    public class Bot2Handler extends Handler {
        public void handleMessage(Message m) {
            int position;
            Message msgToUI = uiThreadHanler.obtainMessage(BOT2_COMPLETE);
            switch (m.what) {
                case BOT2_MAKEMOVE:
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        System.out.println("Thread interrupted!");
                    }
                    do {
                        position = genRand();
                    } while(!readBoard(position).equals(""));
                    msgToUI.arg1 = position;
                    uiThreadHanler.sendMessage(msgToUI);
                    break;

                case RESTART:
                    bot2Handler.removeCallbacksAndMessages(null);
                    Message msgBot2RestartComplete = uiThreadHanler.obtainMessage(BOT2_RESTART_COMPLETE);
                    uiThreadHanler.sendMessageAtFrontOfQueue(msgBot2RestartComplete);
                    break;
            }
        }
    }

    public void writeBoard(int i, String s) {
        synchronized (board) {
            board[i] = s;
        }
    }

    public void updateCell(int i, String s) {
        synchronized (cells) {
            cells[i].setText(s);
        }
    }

    public String readBoard(int i) {
        synchronized (board) {
            return board[i];
        }
    }

    public int genRand() {
        Random random = new Random();
        return random.nextInt(9);
    }

    public boolean isWon(){
        if((readBoard(0).equals(readBoard(1)) && readBoard(1).equals(readBoard(2)) && !readBoard(0).equals("")) ||
                (readBoard(3).equals(readBoard(4)) && readBoard(4).equals(readBoard(5)) && !readBoard(3).equals("")) ||
                (readBoard(6).equals(readBoard(7)) && readBoard(7).equals(readBoard(8)) && !readBoard(6).equals("")) ||
                (readBoard(0).equals(readBoard(3)) && readBoard(3).equals(readBoard(6)) && !readBoard(0).equals("")) ||
                (readBoard(1).equals(readBoard(4)) && readBoard(4).equals(readBoard(7)) && !readBoard(1).equals("")) ||
                (readBoard(2).equals(readBoard(5)) && readBoard(5).equals(readBoard(8)) && !readBoard(2).equals("")) ||
                (readBoard(0).equals(readBoard(4)) && readBoard(4).equals(readBoard(8)) && !readBoard(0).equals("")) ||
                (readBoard(2).equals(readBoard(4)) && readBoard(4).equals(readBoard(6)) && !readBoard(2).equals(""))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(bot1.isAlive() && bot2.isAlive()) {
            try {
                bot1.interrupt();
                bot2.interrupt();
                uiThreadHanler.removeCallbacksAndMessages(null);
                bot1Handler.removeCallbacksAndMessages(null);
                bot2Handler.removeCallbacksAndMessages(null);
                bot1Handler.getLooper().quitSafely();
                bot2Handler.getLooper().quitSafely();
            } catch (SecurityException e) {
                System.out.println("Threads cannot be interrupted!");
            }
        }
        finish();
    }
}
