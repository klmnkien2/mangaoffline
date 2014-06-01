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
import android.widget.TextView;

public class ChapterAdapter extends BaseAdapter {

	private Context mContext;
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
            holder.button = (TextView) convertView.findViewById(R.id.buttonView);
            holder.name = (TextView) convertView.findViewById(R.id.textView);
            holder.sub = (TextView) convertView.findViewById(R.id.subTextView);
            
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        
        final int pos = position;
        holder.button.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                ((MangaActivity)mContext).downloadChapter(items.get(pos).getChapterUrl());
            }
        });
        holder.name.setText(items.get(position).getTitle());
        holder.sub.setText(items.get(position).getSub());

        if(items.get(position).getIsRead() == 1) {
            holder.name.setTypeface(null, Typeface.ITALIC);
            holder.sub.setTypeface(null, Typeface.ITALIC);
        }
       
        convertView.setOnClickListener(new OnItemClickListener(items.get(position).getChapterUrl()));        
        
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

    public static class ViewHolder {
        public TextView name;
        public TextView sub;
        public TextView button;
    }

	public void clear() {
	    items.clear();
		notifyDataSetChanged();
	}
}
