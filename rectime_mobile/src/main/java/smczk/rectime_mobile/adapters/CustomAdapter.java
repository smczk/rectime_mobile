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

public class CustomAdapter extends ArrayAdapter<String> {

    static class ViewHolder {
        TextView text;
    }

    private LayoutInflater inflater;

    public CustomAdapter(Context context, int textViewResourceId, ArrayList<String> textList) {
        super(context, textViewResourceId, textList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        View view = convertView;

        if (view == null) {
            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.row, null);
            TextView label = (TextView)view.findViewById(R.id.row);
            holder = new ViewHolder();
            holder.text = label;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        String str = getItem(position);

        if (!TextUtils.isEmpty(str)) {
            holder.text.setText(str);
        }

        return view;
    }
}
