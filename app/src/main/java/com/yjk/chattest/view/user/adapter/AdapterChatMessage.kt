package com.yjk.chattest.view.user.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yjk.chattest.data.ChatConstants
import com.yjk.chattest.data.chat.ChatData
import com.yjk.chattest.databinding.MessageNormalBinding

class AdapterChatMessage(val myId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var list = mutableListOf<ChatData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ChatConstants.TYPE_NORMAL_MESSAGE -> {
                return NormalMessageViewHolder(
                    MessageNormalBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            ChatConstants.TYPE_FILE_MESSAGE -> {

            }
            ChatConstants.TYPE_BUY_OTC_MESSAGE -> {

            }
        }

        return NormalMessageViewHolder(
            MessageNormalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list.get(position)
        if (holder is NormalMessageViewHolder) {
            holder.onBind(myId, data)
        }
        // todo - file, otc...
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list.get(position).type
    }

    fun addItem(item: ChatData) {
        this.list.add(item)
        notifyItemInserted(list.size)
    }

    fun addList(list: MutableList<ChatData>) {
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun setList(list: MutableList<ChatData>) {
        this.list = list
        notifyDataSetChanged()
    }

    class NormalMessageViewHolder(val binding: MessageNormalBinding) : RecyclerView.ViewHolder(binding.root) {

        fun onBind(myId: String, chatData: ChatData) {
            val data = chatData.data
            if (data.sender == null || myId != data.sender!!.userId){ // 상대방 메세지
                binding.messageOfMe.visibility = View.GONE
                binding.messageOfYou.visibility = View.VISIBLE

                binding.tvMessageOfYou.text = data.message
//                binding.tvTimeOfMe.text = data.updatedAt
            }else {
                binding.messageOfMe.visibility = View.VISIBLE
                binding.messageOfYou.visibility = View.GONE

                binding.tvMessageOfMe.text = data.message
            }
        }
    }

    fun clear(){
        list.clear()
    }


}