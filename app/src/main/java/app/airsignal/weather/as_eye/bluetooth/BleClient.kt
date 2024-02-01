package app.airsignal.weather.as_eye.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import app.airsignal.weather.util.TimberUtil
import app.airsignal.weather.util.ToastUtils
import app.airsignal.weather.view.perm.RequestPermissionsUtil
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class BleClient(private val activity: Activity) {
    private val instance: BleManager by lazy { BleManager.getInstance() }
    var device: BleDevice? = null
    var serial = "Unknown"
    var scanning = false

    fun getInstance(): BleClient {
        instance.run {
            init(activity.application)
            enableLog(true)
            splitWriteNum = 20
            connectOverTime = 6000
            operateTimeout = 10000
            enableBluetooth()
        }

        return this
    }

    fun isConnected(): Boolean {
        device?.let {
            return instance.isConnected(it)
        } ?: return false
    }

    private fun getGatt(): BluetoothGatt? {
        device?.let {
            return instance.getBluetoothGatt(it)
        } ?: return null
    }

    fun startScan(scanCallback: BleScanCallback) {
        if (!instance.isSupportBle) {
            makeToast(activity, "블루투스 기능을 확인해 주세요")
        } else {
            val perm = RequestPermissionsUtil(activity)
            if (perm.isGrantBle()) {
                scanLeDevice(scanCallback)
            } else {
                makeToast(activity, "권한이 거부 상태입니다")
                perm.requestBlePermissions()
            }
        }
    }

    private fun scanLeDevice(scanCallback: BleScanCallback) {
        if (instance.isSupportBle) {
            if (!scanning) {
                scanning = true
                instance.scan(scanCallback)
            }
        } else {
            makeToast(activity, "BLE 미지원 디바이스")
        }
    }

    fun destroyBle() {
        instance.run {
            disconnectAllDevice()
            disableBluetooth()
            destroy()
        }
    }

    fun cancelScan() {
        scanning = false
        instance.cancelScan()
    }

    fun connectDevice(device: BleDevice, connectCallback: BleGattCallback) {
        instance.connect(device, connectCallback)
    }

    fun disconnect() {
        device?.let {
            if (isConnected()) {
                instance.disconnect(device)
            }
        }
    }

    fun postSsid(pwd: String, writePwdCallback: BleWriteCallback) {
        val gatt = getGatt()
        gatt?.let {
            gatt.services.forEach { service ->
                service.characteristics.forEach { char ->
                    val parse = parseProperty(char.properties)
                    if (parse == "WRITE" ||
                        parse == "WRITE NO RESPONSE" ||
                        parse == "READ & WRITE"
                    ) {
                        when (val uuid = char.uuid.toString()) {
                            "37BE4E09-776F-4265-A6EE-6F3396E26639".lowercase(
                                Locale.getDefault()
                            ) -> {
                                TimberUtil().d("testtest", "find write ssid service - $serial")
                                instance.write(
                                    device,
                                    service.uuid.toString(),
                                    uuid,
                                    serial.toByteArray(),
                                    object : BleWriteCallback() {
                                        override fun onWriteSuccess(
                                            current: Int,
                                            total: Int,
                                            justWrite: ByteArray?
                                        ) {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                instance.removeConnectGattCallback(device)
                                                delay(2000)
                                                postPwd(pwd, writePwdCallback)
                                            }
                                        }

                                        override fun onWriteFailure(exception: BleException?) {
                                            device?.let {
                                                instance.disconnect(it)
                                            }
                                            TimberUtil().e(
                                                "testtest",
                                                "onWriteFailure is ${exception?.description}"
                                            )
                                        }
                                    }
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    fun postPwd(pwd: String, writeCallback: BleWriteCallback) {
        val gatt = getGatt()
        gatt?.let {
            gatt.services.forEach { service ->
                service.characteristics.forEach { char ->
                    val parse = parseProperty(char.properties)
                    if (parse == "WRITE" || parse == "WRITE NO RESPONSE" ||
                        parse == "READ & WRITE"
                    ) {
                        when (val uuid = char.uuid.toString()) {
                            "557E202B-A185-4098-B5FB-BA1B61F699EA".lowercase(
                                Locale.getDefault()
                            ) -> {
                                TimberUtil().d("testtest", "find write pwd service - $pwd")
                                instance.write(
                                    device,
                                    service.uuid.toString(),
                                    uuid,
                                    pwd.toByteArray(),
                                    writeCallback
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    fun readConnected(readCallback: BleReadCallback) {
        val gatt = getGatt()
        gatt?.let {
            it.services.forEach { service ->
                service.characteristics.forEach { char ->
                    if (parseProperty(char.properties) == "READ" ||
                        parseProperty(char.properties) == "READ & WRITE"
                    ) {
                        val uuid = char.uuid.toString()
                        if (uuid == "FC245120-55EC-4508-8BA8-A7B1C448C5ED".lowercase(Locale.getDefault())
                        ) {
                            instance.read(
                                device,
                                service.uuid.toString(),
                                uuid,
                                readCallback
                            )
                        }
                    }
                }
            }
        }
    }

    private fun makeToast(context: Context, s: String) {
        ToastUtils(context).showMessage(s)
    }

    private fun parseProperty(i: Int): String {
        return when (i) {
            BluetoothGattCharacteristic.PROPERTY_NOTIFY -> {
                "NOTIFY"
            }
            BluetoothGattCharacteristic.PROPERTY_READ -> {
                "READ"
            }
            BluetoothGattCharacteristic.PROPERTY_WRITE -> {
                "WRITE"
            }
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE -> {
                "WRITE NO RESPONSE"
            }
            BluetoothGattCharacteristic.PROPERTY_BROADCAST -> {
                "BROADCAST"
            }
            BluetoothGattCharacteristic.PROPERTY_INDICATE -> {
                "INDICATE"
            }
            BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS -> {
                "EXTENDED_PROPS"
            }
            BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE -> {
                "SIGNED WRITE"
            }
            10 -> {
                "READ & WRITE"
            }
            else -> {
                "UNKNOWN $i"
            }
        }
    }
}