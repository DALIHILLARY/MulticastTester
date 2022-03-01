package ug.ssema.multicasttest

import android.provider.Telephony.Carriers.PORT
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.net.*
import java.util.*

class MainActivityViewModel : ViewModel(){
    var address: String  = ""
    var port : Int =  33456
    var isP2pReceiver = false
    var isWlanReceiver = false
    var isP2pSend = false
    private val consoleTextFlow = MutableStateFlow("HIX welcomes you!!!...\nHappy Testing\n..........................")
    val consoleTextLiveData  = consoleTextFlow.asSharedFlow()
    private val p2p0 = NetworkInterface.getByName("p2p0")
    private val wlan0 = NetworkInterface.getByName("wlan0")
    private val ap0 = NetworkInterface.getByName("ap0")
    private lateinit var group: InetAddress
    private lateinit var socketAddress: InetSocketAddress
    private lateinit var wlanSocket: MulticastSocket
    private lateinit var p2pSocket: MulticastSocket
    private val scope = viewModelScope
    private val TAG = javaClass.simpleName
    private lateinit var wlanJob: Job
    private lateinit var p2pJob: Job

    fun addressGen() {
        viewModelScope.launch(Dispatchers.IO) {
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

            val string = "${consoleTextFlow.value}\nNew Multicast Address: $address  Port: $port"
            consoleTextFlow.emit(string)
            group = InetAddress.getByName(address)
            socketAddress = InetSocketAddress(group,port)
            wlanSocket = MulticastSocket(port)
            p2pSocket = MulticastSocket(port)


        }

    }
    fun startP2pReceiver() {
        if(!isP2pReceiver) {
            p2pJob = scope.launch(Dispatchers.IO){
                try{
                    p2pSocket.joinGroup(socketAddress,p2p0)
                    isP2pReceiver = true
                    var string = "${consoleTextFlow.value}\np2p0 receiver Started"
                    consoleTextFlow.emit(string)
                    while(isActive){
                        val rBuffer = ByteArray(1200)
                        val packet = DatagramPacket(rBuffer, rBuffer.size)
                        try{
                            p2pSocket.receive(packet)
                            Log.e("p2p0receiver","p2p0 data received")
                            consoleTextFlow.value
                            string = "${consoleTextFlow.value}\nRECEIVED p2p0: ${String(packet.data)}"
                            consoleTextFlow.emit(string)
                        }catch(e: SocketException) {
                            Log.e(TAG,"Error in p2p receiver",e)
                            break
                        }

                    }
                }catch (e: IOException) {
                    val string = "${consoleTextFlow.value}\nPlease Enable Wifi Direct"
                    consoleTextFlow.emit(string)
                }
            }
        }

    }
    fun startWlan0Receiver() {
        if(!isWlanReceiver)  {
            wlanJob = scope.launch(Dispatchers.IO) {
                try{
                    wlanSocket.joinGroup(socketAddress,wlan0)
                    isWlanReceiver = true
                    var string = "${consoleTextFlow.value}\nwlan0 receiver Started"
                    consoleTextFlow.emit(string)
                    while(isActive) {
                        val rBuffer = ByteArray(1200)
                        val packet = DatagramPacket(rBuffer, rBuffer.size)
                        try{
                            wlanSocket.receive(packet)
                            Log.e("wlan0receiver","wlan0 data received")
                            string = "${consoleTextFlow.value}\nRECEIVED wlan0: ${String(packet.data)}"
                            consoleTextFlow.emit(string)
                        }catch(e: SocketException) {
                            Log.e(TAG,"Error in wlan0 receiver",e)
                            break
                        }

                    }
                }catch (e: IOException){
                    val string = "${consoleTextFlow.value}\nPlease Enable Wifi"
                    consoleTextFlow.emit(string)
                }
            }
        }

    }
    fun stopP2pReceiver()  {
        if(isP2pReceiver)  {
            viewModelScope.launch(Dispatchers.IO) {
                var string = ""
                try{
                    p2pSocket.leaveGroup(socketAddress,p2p0)
                    p2pJob.cancel("p2p socket receiver cancelled")
                    isP2pReceiver = false
                    string = "${consoleTextFlow.value}\np2p0 receiver Stopped"
                }catch (e: Throwable){
                    string = "${consoleTextFlow.value}\nSomething Went Wrong"
                }
                consoleTextFlow.emit(string)

            }
        }

    }
    fun stopWlan0Receiver() {
        if(isWlanReceiver) {
            viewModelScope.launch(Dispatchers.IO) {
                var string = ""
                try{
                    wlanSocket.leaveGroup(socketAddress,  wlan0)
                    wlanJob.cancel("wlan0 socket receiver cancelled")
                    isWlanReceiver = false
                    string = "${consoleTextFlow.value}\nWlan0 receiver Stopped"
                }catch (e: Throwable){
                    string = "${consoleTextFlow.value}\nSomething Went Wrong"
                }
                consoleTextFlow.emit(string)
            }
        }

    }
    fun sendData(address: String, message: String,port: String) {
        var string  = ""
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val receiver = InetAddress.getByName(address)
                val payload = DatagramPacket(message.toByteArray(), message.length, receiver, port.toInt())
                if (!isP2pSend) {
                    wlanSocket.networkInterface = wlan0
                    wlanSocket.send(payload)
                    Log.d(TAG, "Sending via wlan0")
                    string = "${consoleTextFlow.value}\nSENT via wlan0: $message"

                } else {
                    p2pSocket.networkInterface = p2p0
                    p2pSocket.send(payload)
                    Log.d(TAG, "Sending via p2p0")
                    string = "${consoleTextFlow.value}\nSENT via p2p0: $message"

                }

            } catch (e: Throwable) {
                string = "${consoleTextFlow.value}\nError occurred when sending  data"

                Log.e(TAG, "ERROR OCCURRED WHILE SENDING", e)
            }
            consoleTextFlow.emit(string)

        }
    }
}