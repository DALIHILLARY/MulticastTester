package ug.ssema.multicasttest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.*
import java.util.*

class MainActivityViewModel : ViewModel(){
    var address: String  = ""
    var port : Int =  33456
    var isP2pReceiver = false
    var isP2pSend = false
    var consoleText = "Hello There!!1...\nHappy Testing\n.........................."
    private val p2p0 = NetworkInterface.getByName("p2p0")
    private val wlan0 = NetworkInterface.getByName("wlan0")
    private val ap0 = NetworkInterface.getByName("ap0")
    private lateinit var group: InetAddress
    private lateinit var socketAddress: InetSocketAddress
    private lateinit var wlanSocket: MulticastSocket
    private lateinit var p2pSocket: MulticastSocket
    private val scope = viewModelScope
    private val TAG = javaClass.simpleName
    fun addressGen() {
        val numberList = mutableListOf<String>()
        var count = 0
        while(true){
            count += 1
            if(count > 3){
                break
            }
            val number = (1 + Random().nextInt(254 - 1)).toString()
            numberList.add(number)
        }
        address =  "230." + numberList.joinToString(separator = ".")
        viewModelScope.launch(Dispatchers.IO) {
            group = InetAddress.getByName(address)
            socketAddress = InetSocketAddress(group,port)
            startWlan0Receiver()
        }
    }
    fun startP2pReceiver() {

    }
    fun startWlan0Receiver() {
        wlanSocket = MulticastSocket(port)
        scope.launch(Dispatchers.IO) {
            wlanSocket.joinGroup(socketAddress,wlan0)
            while(true) {
                val rBuffer = ByteArray(1200)
                val packet = DatagramPacket(rBuffer, rBuffer.size)
                try{
                    wlanSocket.receive(packet)
                    Log.e("wlan0receiver","wlan0 data received")

                }catch(e: SocketException) {
                    Log.e(TAG,"Error in wlan0 receiver",e)
                    break
                }

            }
        }
    }
    fun stopP2pReceiver()  {

    }
    fun stopWlan0Receiver() {

    }
}