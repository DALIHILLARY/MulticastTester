package ug.ssema.multicasttest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.net.NetworkInterface

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel: MainActivityViewModel by viewModels()
        val console : TextView =  findViewById(R.id.output_textView)
        val wlanReceiver :  SwitchMaterial = findViewById(R.id.wlan_receiver)
        val p2pReceiver : SwitchMaterial = findViewById(R.id.p2p_receiver)
        val addressValue : TextInputEditText = findViewById(R.id.address_value)
//        val portValue : TextInputEditText = findViewById(R.id.)
        val sendInterface : SwitchMaterial  = findViewById (R.id.interfaceToggle)
        wlanReceiver.isChecked  = viewModel.isP2pReceiver
        sendInterface.isChecked = viewModel.isP2pSend
        if(viewModel.address.isEmpty() )  {
            viewModel.addressGen()
            viewModel.consoleText += "\nNew Multicast Address: ${viewModel.address}  Port: ${viewModel.port}"
            console.text = viewModel.consoleText
        }
         wlanReceiver.setOnCheckedChangeListener { _, b ->
             if(b){
                viewModel.isP2pReceiver = true

             }else{
                viewModel.isP2pReceiver = false
             }
         }
        p2pReceiver.setOnCheckedChangeListener { _, b ->
            if(b){
                viewModel.isP2pSend  = true
            }else{
                viewModel.isP2pSend = false
            }
        }

    }
}