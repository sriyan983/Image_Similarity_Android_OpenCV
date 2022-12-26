package utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.InputType

import android.widget.EditText



interface OnDialogPressListenerInterface {
    fun okClick(numberOfImages: Int)
    fun cancelClick()
}
class Dialog {
    private val m_Text = ""
    fun showDialog(context: Context, callback: OnDialogPressListenerInterface){
        val txtUrl = EditText(context)
        txtUrl.inputType = InputType.TYPE_CLASS_NUMBER

        // Set the default text to a link of the Queen
        txtUrl.hint = "#enter a number"

        AlertDialog.Builder(context)
            .setTitle("Create new set")
            .setMessage("Enter the number of images you would like to capture.")
            .setView(txtUrl)
            .setPositiveButton("Start",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    val str = txtUrl.text.toString()
                    val parsedInt = Integer.parseInt(str)
                    callback.okClick(parsedInt)
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    callback.cancelClick()
                })
            .show()
    }
}