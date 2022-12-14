package com.ke.hs_tracker.module

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.documentfile.provider.DocumentFile
import com.bumptech.glide.Glide
import com.ke.hs_tracker.module.data.PreferenceStorage
import com.ke.hs_tracker.module.databinding.ModuleDialogCardPreviewBinding
import com.ke.hs_tracker.module.databinding.ModuleItemCardBinding
import com.ke.hs_tracker.module.entity.Card
import com.ke.hs_tracker.module.parser.PowerParserImpl
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.tencent.bugly.crashreport.CrashReport
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject


@HiltAndroidApp
class MainApplication : Application() {


    @Inject
    lateinit var preferenceStorage: PreferenceStorage

    override fun onCreate() {
        super.onCreate()
        CrashReport.initCrashReport(applicationContext, "abb84be20b", false)
        AppCompatDelegate.setDefaultNightMode(preferenceStorage.theme)
        Logger.addLogAdapter(
            AndroidLogAdapter(
                PrettyFormatStrategy.newBuilder()
                    .methodCount(5)
                    .build()
            )
        )
        Build.BRAND.log()
    }
}

fun String.removeTime(): Triple<String, Date, String> {

    val content = substring(PowerParserImpl.TIME_PREFIX_SIZE)
    val start = substring(0, 1)
    val hms = substring(2, 10).split(":")
    val calendar = Calendar.getInstance()
    calendar.set(
        Calendar.HOUR_OF_DAY, hms[0].toInt()
    )
    calendar.set(
        Calendar.MINUTE, hms[1].toInt()
    )
    calendar.set(
        Calendar.SECOND, hms[2].toInt()
    )

    return Triple(
        start,
        calendar.time,
        content
    )
}


fun String.log() {
    Logger.d(this)
}

/**
 * ????????????????????????
 */
val Context.hasAllPermissions: Boolean
    get() {
        val canWriteExternalStorage = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        return canWriteExternalStorage && canReadDataDir && isExternalStorageManager()
    }

/**
 * ??????????????????data??????
 */
val Context.canReadDataDir: Boolean
    get() {
        return DocumentFile.fromTreeUri(applicationContext, DATA_DIR_URI)?.canRead() ?: false
    }

/**
 * ?????????????????????????????????????????????android????????????11?????????true
 */
fun isExternalStorageManager(): Boolean {

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        return true
    }
}

val DATA_DIR_URI =
    Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")!!

const val HS_APPLICATION_ID = "com.blizzard.wtcg.hearthstone"

const val HUAWEI_HS_APPLICATION_ID = "com.blizzard.wtcg.hearthstone.cn.huawei"

/**
 * ??????log.config??????
 */
suspend fun Context.writeLogConfigFile(forceWrite: Boolean = false): Boolean {
    return withContext(Dispatchers.IO) {


        val documentFile = findHSDataFilesDir() ?: return@withContext false

        val fileName = "log.config"
        val file = findHSDataFilesDir(fileName)
        if (file != null) {
            //???????????????
            "log.config???????????????".log()
            if (forceWrite) {
                file.delete()
            } else {
                return@withContext true
            }
        }
        val configFile = documentFile.createFile("plain/text", fileName)
            ?: return@withContext false

        contentResolver.openOutputStream(configFile.uri)?.use {
            assets.open("log.config")
                .copyTo(it)
            it.flush()
        }

        return@withContext true
    }


}

//???????????????????????????????????????????????????
fun Context.findHSDataFilesDir(

    fileName: String? = null,
    applicationId: String
    = HS_APPLICATION_ID,
): DocumentFile? {

    DocumentFile.fromTreeUri(
        applicationContext,
        DATA_DIR_URI
    )?.apply {
        listFiles().forEach {
            if (it.name == applicationId) {
                val filesDir = it.findFile("files") ?: return null
                return if (fileName == null) filesDir else filesDir.findFile(
                    fileName
                )
            }
        }
    }

    if (applicationId == HUAWEI_HS_APPLICATION_ID) {
        return null
    }

    return findHSDataFilesDir(fileName, HUAWEI_HS_APPLICATION_ID)


}

fun ModuleItemCardBinding.bindCard(card: Card) {
    name.text = card.name
    cost.text = card.cost.toString()

    card.rarity?.apply {
        this@bindCard.cost.setBackgroundColor(
            ResourcesCompat.getColor(
                root.context.resources,
                colorRes,
                null
            )
        )

    }

    Glide.with(imageTile)
        .load("https://art.hearthstonejson.com/v1/tiles/${card.id}.png")
        .into(imageTile)
}


fun showCardImageDialog(context: Context, cardId: String) {
    val binding = ModuleDialogCardPreviewBinding.inflate(LayoutInflater.from(context))
    AlertDialog.Builder(context)
        .show().apply {
            window?.run {
                setContentView(binding.root)
                binding.root.setOnClickListener {
                    dismiss()
                }
                //??????????????????????????????
                setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
    Glide.with(binding.image)
        .load("https://art.hearthstonejson.com/v1/render/latest/zhCN/512x/${cardId}.png")
        .placeholder(R.mipmap.ic_launcher)
        .into(binding.image)
}
