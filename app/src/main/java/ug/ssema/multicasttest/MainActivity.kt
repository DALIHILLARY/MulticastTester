package ug.ssema.multicasttest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.NetworkInterface

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel: MainActivityViewModel by viewModels()
        val mScrollView : ScrollView =  findViewById(R.id.output_textView)
        val mConsoleText : TextView = findViewById(R.id.console_text)
        val wlanReceiver :  SwitchMaterial = findViewById(R.id.wlan_receiver)
        val p2pReceiver : SwitchMaterial = findViewById(R.id.p2p_receiver)
        val addressValue : TextInputEditText = findViewById(R.id.address_value)
        val sendButton  : Button = findViewById(R.id.sendButton)
        val messageField : TextInputLayout = findViewById(R.id.messageField)
        val addressField : TextInputLayout = findViewById(R.id.addressField)
        val messageValue : TextInputEditText = findViewById(R.id.message_value)
        val portValue : TextInputEditText = findViewById(R.id.port_value)
        val portField : TextInputLayout = findViewById(R.id.portField)
        val sendInterface : SwitchMaterial  = findViewById (R.id.interfaceToggle)
        wlanReceiver.isChecked  = viewModel.isWlanReceiver
        p2pReceiver.isChecked = viewModel.isP2pReceiver
        sendInterface.isChecked = viewModel.isP2pSend

        launch {
            viewModel.consoleTextLiveData.collectLatest {
                mConsoleText.text = it
                mScrollView.smoothScrollTo(0, mConsoleText.bottom);
            }
        }
        if(viewModel.address.isEmpty() )  {
            viewModel.addressGen()
        }
         wlanReceiver.setOnCheckedChangeListener { _, b ->
             if(b){
                viewModel.startWlan0Receiver()

             }else{
                viewModel.stopWlan0Receiver()
             }
         }
        p2pReceiver.setOnCheckedChangeListener { _, b ->
            if(b){
                viewModel.startP2pReceiver()
            }else{
                viewModel.stopP2pReceiver()
            }
        }
        sendInterface.setOnCheckedChangeListener { _, boolean ->
            viewModel.isP2pSend =  boolean
        }
        sendButton.setOnClickListener {
            val toAddress = addressValue.text.toString()
            val message = messageValue.text.toString()
            val port = portValue.text.toString()
            if(message.isNotEmpty() && toAddress.isNotEmpty() && port.isNotEmpty()) {
                 viewModel.sendData(toAddress,message,port)
            }else{
                if(message.isEmpty()) messageField.error = "Message Empty"
                if(toAddress.isEmpty()) addressField.error = "Invalid Address"
                if(port.isEmpty()) portField.error = "Invalid Port"
            }

        }



    }
}