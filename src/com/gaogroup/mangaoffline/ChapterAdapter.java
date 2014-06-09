package com.gaogroup.mangaoffline;

import java.util.ArrayList;
import java.util.List;

import com.gaogroup.mangaoffline.model.ChapterInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChapterAdapter extends ArrayAdapter<ChapterInfo> {

	private Context mContext;
	private int layoutResourceId;
	private ArrayList<ChapterInfo> items = new ArrayList<ChapterInfo>();

    public static class ViewHolder {
        public TextView name;
        public TextView sub;
        public ImageView button;
        public ProgressBar progressBar;
        public TextView progressText;
    }

	public ChapterAdapter(Context c, int layoutResourceId,  ArrayList<ChapterInfo> items) {
	    super(c, layoutResourceId, items);
	    this.items = items;
		this.layoutResourceId = layoutResourceId;
		mContext = c;
	}	
	
	public void addAll(List<ChapterInfo> adds) {
	    for (ChapterInfo info : adds) {
	        add(info);
	    }
	}

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        
        ViewHolder holder;
        final ChapterInfo info = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.button = (ImageView) convertView.findViewById(R.id.buttonView);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.downloadProgressBar);
            holder.progressText = (TextView) convertView.findViewById(R.id.downloadProgressText);
            holder.name = (TextView) convertView.findViewById(R.id.textView);
            holder.sub = (TextView) convertView.findViewById(R.id.subTextView);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        holder.progressBar.setProgress(info.getProgress());
        holder.progressText.setText(info.getProgress() + "/" + holder.progressBar.getMax());
        info.setProgressBar(holder.progressBar);
        info.setDownloadButton(holder.button);
        info.setProgressText(holder.progressText);
        
        if(info.getDownloaded() == 1) {
            holder.progressText.setText("Downloaded");
        }
        
        holder.button.setVisibility(info.isDownloading()?View.GONE:View.VISIBLE);
        holder.progressBar.setVisibility(info.isDownloading()?View.VISIBLE:View.GONE);
        holder.progressText.setVisibility((info.isDownloading() || info.getDownloaded() == 1)?View.VISIBLE:View.GONE);
        
        final ImageView button = holder.button;
        final ProgressBar progressBar = holder.progressBar;
        final TextView progressText = holder.progressText;
        holder.button.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if(info.getDownloaded() == 1) confirmDownload(info);
                else {                
                    button.setVisibility(View.GONE);
                    button.invalidate();
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.invalidate();
                    progressText.setVisibility(View.VISIBLE);
                    progressText.invalidate();
                    info.setDownloading(true);
                    
                    ((MangaActivity)mContext).downloadChapter(info);
                }
            }
        });
        
        holder.name.setText(items.get(position).getTitle());
        holder.sub.setText(items.get(position).getSub());

        if(info.getIsRead() == 1) {
            holder.name.setTypeface(null, Typeface.ITALIC);
            holder.sub.setTypeface(null, Typeface.ITALIC);
        }
       
        convertView.setOnClickListener(new OnItemClickListener(info.getChapterUrl()));        
        
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
    
    public void confirmDownload(final ChapterInfo info) {

        AlertDialog.Builder confirm = new AlertDialog.Builder(mContext);
        confirm.setTitle("Confirm");
        confirm.setMessage("This chapter had downloaded once. Do you really wanna try again?");

        confirm.setNegativeButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface downloadDialog, int which) {

                info.getDownloadButton().setVisibility(View.GONE);
                info.getDownloadButton().invalidate();
                info.getProgressBar().setVisibility(View.VISIBLE);
                info.getProgressBar().invalidate();
                info.setDownloading(true);
                
                ((MangaActivity)mContext).downloadChapter(info);
            }
        });
        
        confirm.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        confirm.show().show();
    }

    @Override
	public void clear() {
	    super.clear();
		notifyDataSetChanged();
	}
}
