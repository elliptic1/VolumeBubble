/*
 * Copyright Txus Ballesteros 2015 (@txusballesteros)
 *
 * This file is part of some open source application.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contact: Txus Ballesteros <txus.ballesteros@gmail.com>
 */
package com.txusballesteros.bubbles;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;
import com.txusballesteros.bubbles.R;

import java.util.ArrayList;
import java.util.List;

public class BubblesService extends Service {
    private BubblesServiceBinder binder = new BubblesServiceBinder();
    private List<BubbleLayout> bubbles = new ArrayList<>();
    private BubbleTrashLayout bubblesTrash;
    private WindowManager windowManager;
    private BubblesLayoutCoordinator layoutCoordinator;

    private static final String CHANNEL_ID = "bubble_service";
    private static final int NOTIFICATION_ID = 42;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        String title = getApplicationInfo().loadLabel(getPackageManager()).toString();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(getString(R.string.bubble_service_running))
                .setSmallIcon(getApplicationInfo().icon)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void recycleBubble(final BubbleLayout bubble) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                getWindowManager().removeView(bubble);
                for (BubbleLayout cachedBubble : bubbles) {
                    if (cachedBubble == bubble) {
                        bubble.notifyBubbleRemoved();
                        bubbles.remove(cachedBubble);
                        break;
                    }
                }
            }
        });
    }

    private WindowManager getWindowManager() {
        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        return windowManager;
    }

    public void addBubble(BubbleLayout bubble, int x, int y) {
        WindowManager.LayoutParams layoutParams = buildLayoutParamsForBubble(x, y);
        bubble.setWindowManager(getWindowManager());
        bubble.setViewParams(layoutParams);
        bubble.setLayoutCoordinator(layoutCoordinator);
        bubbles.add(bubble);
        addViewToWindow(bubble);
    }

    void addTrash(int trashLayoutResourceId) {
        if (trashLayoutResourceId != 0) {
            bubblesTrash = new BubbleTrashLayout(this);
            bubblesTrash.setWindowManager(windowManager);
            bubblesTrash.setViewParams(buildLayoutParamsForTrash());
            bubblesTrash.setVisibility(View.GONE);
            LayoutInflater.from(this).inflate(trashLayoutResourceId, bubblesTrash, true);
            addViewToWindow(bubblesTrash);
            initializeLayoutCoordinator();
        }
    }

    private void initializeLayoutCoordinator() {
        layoutCoordinator = new BubblesLayoutCoordinator.Builder(this)
                .setWindowManager(getWindowManager())
                .setTrashView(bubblesTrash)
                .build();
    }

    private void addViewToWindow(final BubbleBaseLayout view) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                getWindowManager().addView(view, view.getViewParams());
            }
        });
    }

    private WindowManager.LayoutParams buildLayoutParamsForBubble(int x, int y) {
        int typeOverlay = WindowManager.LayoutParams.TYPE_PHONE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeOverlay = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                typeOverlay,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = x;
        params.y = y;
        return params;
    }

    private WindowManager.LayoutParams buildLayoutParamsForTrash() {
        int x = 0;
        int y = 0;

        int typeOverlay = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeOverlay = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                typeOverlay,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);
        params.x = x;
        params.y = y;
        return params;
    }

    public void removeBubble(BubbleLayout bubble) {
        recycleBubble(bubble);
    }

    public class BubblesServiceBinder extends Binder {
        public BubblesService getService() {
            return BubblesService.this;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bubble Service",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}