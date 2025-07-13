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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 101
    }

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
        activityMainBinding.about.setOnClickListener { showAboutDialog() }
        initializeBubblesManager()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun noPermissions() {
        activityMainBinding.needPerms.visibility = View.VISIBLE
        activityMainBinding.about.visibility = View.GONE
        activityMainBinding.add.text = getString(R.string.add_bubble)
        activityMainBinding.add.setOnClickListener {
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.overlay_permission_title)
            .setMessage(R.string.overlay_permission_rationale)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                requestOverlayPermission()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showAboutDialog() {
        val view = layoutInflater.inflate(R.layout.content_about, null)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_activity_about)
            .setView(view)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestOverlayPermission() {
        val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        myIntent.data = Uri.parse("package:" + packageName)
        startActivityForResult(myIntent, REQUEST_OVERLAY_PERMISSION)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION && Settings.canDrawOverlays(this)) {
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
        // Use the application context so the bubble doesn't disappear when the
        // activity is recreated or closed
        bubblesManager = BubblesManager.Builder(applicationContext)
            .setTrashLayout(bubble_trash_layout)
            .build()
        bubblesManager?.initialize()
    }

}
