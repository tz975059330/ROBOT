package com.king.myrobot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by 16230 on 2016/8/3.
 */

public class ChatMsgViewAdpater extends BaseAdapter {
    //ListView视图的内容由IMsgViewType决定
    public static interface IMsgViewType
    {
        //对方发来的信息
        int IMVT_COM_MSG = 0;
        //自己发出的信息
        int IMVT_TO_MSG = 1;
    }

    private static final String TAG = ChatMsgViewAdpater.class.getSimpleName();
    private List<ChatMsgEntity> data;
    protected Context context;
    private LayoutInflater mInflater;

    public ChatMsgViewAdpater(Context context, List<ChatMsgEntity> data) {
        this.context = context;
        this.data = data;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return  data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ChatMsgEntity entity = data.get(i);
        boolean isComMsg = entity.isComMeg();

        ViewHolder viewHolder = null;
        if (view == null)
        {
            if (isComMsg)
            {
                //如果是对方发来的消息，则显示的是左气泡
                view = mInflater.inflate(R.layout.chatting_item_msg_text_left, null);
            }else{
                //如果是自己发出的消息，则显示的是右气泡
                view = mInflater.inflate(R.layout.chatting_item_msg_text_right, null);
            }

            viewHolder = new ViewHolder();
            viewHolder.tvSendTime = (TextView) view.findViewById(R.id.tv_sendtime);
            viewHolder.tvUserName = (TextView) view.findViewById(R.id.tv_username);
            viewHolder.tvContent = (TextView) view.findViewById(R.id.tv_chatcontent);
            viewHolder.isComMsg = isComMsg;

            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.tvSendTime.setText(entity.getDate());
        viewHolder.tvUserName.setText(entity.getName());
        viewHolder.tvContent.setText(entity.getText());

        return view;
    }
    //获取项的类型
    public int getItemViewType(int position) {
        ChatMsgEntity entity = data.get(position);

        if (entity.isComMeg())
        {
            return IMsgViewType.IMVT_COM_MSG;
        }else{
            return IMsgViewType.IMVT_TO_MSG;
        }

    }
    //获取项的类型数
    public int getViewTypeCount() {
        // TODO Auto-generated method stub
        return 2;
    }

    //通过ViewHolder显示项的内容
    static class ViewHolder {
        public TextView tvSendTime;
        public TextView tvUserName;
        public TextView tvContent;
        public boolean isComMsg = true;
    }
}
