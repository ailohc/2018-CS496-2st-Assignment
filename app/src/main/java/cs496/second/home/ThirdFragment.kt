package cs496.second.home

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import cs496.second.R


class ThirdFragment : Fragment() {
    companion object {
        fun newInstance(): ThirdFragment {
            val fragment = ThirdFragment()
            return fragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_third, container, false)
        val ChatStartbutton = rootView.findViewById<Button>(R.id.button1)
        ChatStartbutton.setOnClickListener {
            var intent = Intent(context, Third_Sub_Activity::class.java)
            startActivity(intent)
        }
        val ChatbotStartbutton = rootView.findViewById<Button>(R.id.button2)
        ChatbotStartbutton.setOnClickListener {
            var intent2 = Intent(context, FourthActivity::class.java)
            startActivity(intent2)
        }
        return rootView
    }

}