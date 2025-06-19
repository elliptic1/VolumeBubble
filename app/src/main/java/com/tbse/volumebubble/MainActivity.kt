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
package com.tbse.volumebubble

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.tbse.volumebubble.R.layout.*
import com.tbse.volumebubble.databinding.ActivityMainBinding
import com.tbse.volumebubble.databinding.BubbleLayoutBinding
import com.txusballesteros.bubbles.BubbleLayout
import com.txusballesteros.bubbles.BubblesManager


class MainActivity : AppCompatActivity() {

    private var bubblesManager: BubblesManager? = null

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var bubbleLayoutBinding: BubbleLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)

        FirebaseApp.initializeApp(this)

        setContentView(activity_main)

    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                noPermissions()
            } else {
                hasPermissions()
            }
        } else {
            hasPermissions()
        }
    }

    private fun hasPermissions() {
        activityMainBinding.needPerms.visibility = View.GONE
        activityMainBinding.about.visibility = View.VISIBLE
        activityMainBinding.add.text = getString(R.string.add_bubble)
        activityMainBinding.add.visibility = View.VISIBLE
        activityMainBinding.add.setOnClickListener { addNewBubble() }
        activityMainBinding.about.setOnClickListener { startActivity(Intent(this, About::class.java)) }
        initializeBubblesManager()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun noPermissions() {
        activityMainBinding.needPerms.visibility = View.VISIBLE
        activityMainBinding.about.visibility = View.GONE
        activityMainBinding.add.text = getString(R.string.request_permission)
        activityMainBinding.add.setOnClickListener {
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            myIntent.data = Uri.parse("package:" + packageName)
            startActivityForResult(myIntent, 101)
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && Settings.canDrawOverlays(this)) {
            hasPermissions()
        } else {
            noPermissions()
        }
    }

    private fun addNewBubble() {

        val bubbleView = LayoutInflater.from(this@MainActivity).inflate(bubble_layout, null) as BubbleLayout
        bubbleView.setOnBubbleRemoveListener { }
        bubbleView.setOnBubbleClickListener {
            val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI)
        }
        bubbleView.setShouldStickToWall(true)
        bubblesManager?.addBubble(bubbleView, 60, 20)
    }

    private fun initializeBubblesManager() {
        bubblesManager = BubblesManager.Builder(this)
            .setTrashLayout(bubble_trash_layout)
            .build()
        bubblesManager?.initialize()
    }

}
