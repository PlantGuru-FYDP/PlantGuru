package com.example.plantgurucompose.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

fun updateBluetoothState(context:Context, state:String) {
    Toast.makeText(context, state, Toast.LENGTH_SHORT).show()
    Log.d("BluetoothState", state)
}