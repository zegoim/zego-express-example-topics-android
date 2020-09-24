package im.zego.audioeffectplayer;

import android.widget.BaseAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;



import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class AutoCompleteTextViewAdapter extends BaseAdapter implements Filterable {
    private ArrayFilter mFilter;
    private List<String> mList;
    private Context context;
    private ArrayList<String> mUnfilteredData;
    private List<String> tempData;
    public AutoCompleteTextViewAdapter(List<String> mList, Context context) {
        this.mList = mList;
        this.tempData =mList;
        this.context = context;
    }

    @Override
    public int getCount() {

        return mList==null ? 0:mList.size();
    }
    public void addAll(List data){
        mList.addAll(data);
        tempData = mList;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView==null){
            view = View.inflate(context, R.layout.path_item, null);

            holder = new ViewHolder();
            holder.text1 = (TextView) view.findViewById(R.id.text1);


            view.setTag(holder);
        }else{
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        String pc = mList.get(position);

        holder.text1.setText(pc);


        return view;
    }

    static class ViewHolder{
        public TextView text1;

    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null||mUnfilteredData.size()!=tempData.size()) {
                mUnfilteredData = new ArrayList<String>(tempData);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<String> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<String> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<String> newValues = new ArrayList<String>(count);

                for (int i = 0; i < count; i++) {
                    String pc = unfilteredValues.get(i);
                    if (pc != null) {

                        if(pc.startsWith(prefixString)){
                            newValues.add(pc);
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            mList = (List<String>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}