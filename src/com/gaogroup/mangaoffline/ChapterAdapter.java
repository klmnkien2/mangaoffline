package com.gaogroup.mangaoffline;

import java.util.ArrayList;
import java.util.List;

import com.gaogroup.mangaoffline.model.ChapterInfo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChapterAdapter extends BaseAdapter {

	private Context mContext;
	private boolean isLoading = false;
	private LayoutInflater infalter;
	private ArrayList<ChapterInfo> items = new ArrayList<ChapterInfo>();

	public ChapterAdapter(Context c) {
		infalter = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = c;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public ChapterInfo getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addAll(List<ChapterInfo> adds) {

		try {
		    
            this.items.addAll(adds);

		} catch (Exception e) {
			e.printStackTrace();
		}

		notifyDataSetChanged();
	}

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        
        ViewHolder holder;

        if (convertView == null) {
            convertView = infalter.inflate(R.layout.chapter_list_item, null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textView);
            holder.sub = (TextView) convertView.findViewById(R.id.subTextView);
            
            holder.realItem = (LinearLayout) convertView.findViewById(R.id.realItem);
            holder.progressItem = (LinearLayout) convertView.findViewById(R.id.progressItem);

            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        
        if(position == items.size()-1 && this.isLoading) {
            holder.realItem.setVisibility(View.GONE);
            holder.progressItem.setVisibility(View.VISIBLE);
        } else {
            holder.realItem.setVisibility(View.VISIBLE);
            holder.progressItem.setVisibility(View.GONE);
            
            holder.name.setText(items.get(position).getTitle());
            holder.sub.setText(items.get(position).getSub());

            if(items.get(position).getIsRead() == 1) {
                holder.name.setTypeface(null, Typeface.ITALIC);
                holder.sub.setTypeface(null, Typeface.ITALIC);
            }
           
            convertView.setOnClickListener(new OnItemClickListener(items.get(position).getChapterUrl()));
        }
        
		return convertView;        
	}
    
    private class OnItemClickListener implements OnClickListener {
        private String url;

        OnItemClickListener(String url) {
            this.url = url;
        }

        @Override
        public void onClick(View arg0) {
            Intent i = new Intent(mContext, ViewActivity.class); 
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("CHAPTER_URL", url);      
            mContext.startActivity(i); 
        }
    }
    
    public boolean isLoading() {
        return isLoading;
    }
    
    public void enableLoading() {
        this.items.add(new ChapterInfo(null, null, null, null, -1, -1));
        this.isLoading = true;
        notifyDataSetChanged();
    }

    public void disableLoading() {
        this.isLoading = false;
        this.items.remove(items.size()-1);
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        public TextView name;
        public TextView sub;
        public ImageView image;
        
        public LinearLayout realItem;
        public LinearLayout progressItem;
    }

	public void clear() {
	    items.clear();
		notifyDataSetChanged();
	}
}
