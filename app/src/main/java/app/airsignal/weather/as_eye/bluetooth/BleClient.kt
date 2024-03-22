package app.airsignal.weather.as_eye.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.pm.Capability
import app.airsignal.weather.dao.IgnoredKeyFile.UUID_CONNECTING
import app.airsignal.weather.dao.IgnoredKeyFile.UUID_PWD
import app.airsignal.weather.dao.IgnoredKeyFile.UUID_SSID
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
    val instance: BleManager by lazy { BleManager.getInstance() }
    var device: BleDevice? = null
    var serial = "Unknown"
    var scanning = false

    private enum class BleProtocolType {
        NOTIFY, READ, WRITE, WRITE_NO_RESPONSE, BROADCAST, INDICATE,
        EXTENDED_PROPS, SIGNED_WRITE, READ_AND_WRITE, UNKNOWN
    }

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
            disconnect()
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

    fun postSsid(writeSsidCallback: BleWriteCallback) {
        val gatt = getGatt()
        gatt?.let {
            gatt.services.forEach { service ->
                service.characteristics.forEach { char ->
                    val parse = parseProperty(char.properties)
                    if (parse == BleProtocolType.WRITE ||
                        parse == BleProtocolType.WRITE_NO_RESPONSE ||
                        parse == BleProtocolType.READ_AND_WRITE
                    ) {
                        when (val uuid = char.uuid.toString()) {
                            UUID_SSID.lowercase(
                                Locale.getDefault()
                            ) -> {
                                instance.write(
                                    device,
                                    service.uuid.toString(),
                                    uuid,
                                    serial.toByteArray(),
                                    writeSsidCallback
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
                    if (parse == BleProtocolType.WRITE || parse == BleProtocolType.WRITE_NO_RESPONSE ||
                        parse == BleProtocolType.READ_AND_WRITE
                    ) {
                        when (val uuid = char.uuid.toString()) {
                            UUID_PWD.lowercase(
                                Locale.getDefault()
                            ) -> {
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
                    if (parseProperty(char.properties) == BleProtocolType.READ ||
                        parseProperty(char.properties) == BleProtocolType.READ_AND_WRITE
                    ) {
                        val uuid = char.uuid.toString()
                        if (uuid == UUID_CONNECTING.lowercase(Locale.getDefault())
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

    private fun parseProperty(i: Int): BleProtocolType {
        return when (i) {
            BluetoothGattCharacteristic.PROPERTY_NOTIFY -> {
                BleProtocolType.NOTIFY
            }
            BluetoothGattCharacteristic.PROPERTY_READ -> {
                BleProtocolType.READ
            }
            BluetoothGattCharacteristic.PROPERTY_WRITE -> {
                BleProtocolType.WRITE
            }
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE -> {
                BleProtocolType.WRITE_NO_RESPONSE
            }
            BluetoothGattCharacteristic.PROPERTY_BROADCAST -> {
                BleProtocolType.BROADCAST
            }
            BluetoothGattCharacteristic.PROPERTY_INDICATE -> {
                BleProtocolType.INDICATE
            }
            BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS -> {
                BleProtocolType.EXTENDED_PROPS
            }
            BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE -> {
                BleProtocolType.SIGNED_WRITE
            }
            10 -> {
                BleProtocolType.READ_AND_WRITE
            }
            else -> {
                BleProtocolType.UNKNOWN
            }
        }
    }
}