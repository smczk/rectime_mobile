package smczk.rectime_mobile.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import smczk.rectime_mobile.R;

public class CustomAdapter extends ArrayAdapter<ArrayList<String>> {

    static class ViewHolder {
        TextView text1;
        TextView text2;
        TextView text3;
        TextView text4;
    }

    private LayoutInflater inflater;

    public CustomAdapter(Context context, int textViewResourceId, ArrayList<ArrayList<String>> textList) {
        super(context, textViewResourceId, textList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        View view = convertView;

        if (view == null) {
            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row, null);
            TextView label1 = (TextView)view.findViewById(R.id.row1);
            TextView label2 = (TextView)view.findViewById(R.id.row2);
            TextView label3 = (TextView)view.findViewById(R.id.row3);
            TextView label4 = (TextView)view.findViewById(R.id.row4);
            holder = new ViewHolder();
            holder.text1 = label1;
            holder.text2 = label2;
            holder.text3 = label3;
            holder.text4 = label4;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        ArrayList ary = getItem(position);
        String str1 = ary.get(1).toString();
        String str2 = ary.get(0).toString();
        String str3 = ary.get(3).toString();
        String str4 = ary.get(2).toString();

        if (!TextUtils.isEmpty(str1) && !TextUtils.isEmpty(str2)) {
            holder.text1.setText(str1);
            holder.text2.setText(str2);
            holder.text3.setText(str3);
            holder.text4.setText(str4);

        }

        return view;
    }
}
