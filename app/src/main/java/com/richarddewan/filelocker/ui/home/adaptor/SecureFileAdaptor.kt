package com.richarddewan.filelocker.ui.home.adaptor

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.richarddewan.filelocker.R
import com.richarddewan.filelocker.data.SecureFileEntity
import kotlinx.android.synthetic.main.secure_file_list_view.view.*

class SecureFileAdaptor(private val mList: ArrayList<SecureFileEntity>,val onClickListener:(Int) ->Unit):
    RecyclerView.Adapter<SecureFileAdaptor.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.secure_file_list_view,parent,false)
        return ViewHolder(view,onClickListener)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileEntity = mList[position]
        holder.onBind(fileEntity)
    }

    class ViewHolder(itemView: View,val onClickListener:(Int) ->Unit): RecyclerView.ViewHolder(itemView){
        lateinit var data: SecureFileEntity

        fun onBind(fileEntity: SecureFileEntity){
            data = fileEntity
            itemView.txtFileName.text = data.fileName
            itemView.txtFile.text = data.file.toString()
            itemView.txtFileSize.text = data.fileSize

            itemView.setOnClickListener {
                onClickListener(adapterPosition)
            }
        }

    }

}