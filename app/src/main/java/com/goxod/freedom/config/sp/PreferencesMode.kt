package com.goxod.freedom.config.sp

import android.annotation.SuppressLint
import android.content.Context.*
import android.os.ParcelFileDescriptor.MODE_WORLD_READABLE
import androidx.annotation.IntDef

@SuppressLint("WorldReadableFiles", "WorldWriteableFiles")
@IntDef(flag = true, value = [MODE_PRIVATE, MODE_WORLD_READABLE, MODE_WORLD_WRITEABLE, MODE_MULTI_PROCESS])
annotation class PreferencesMode