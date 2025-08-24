package qn.app.kidsafe_android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UrlAdapter extends RecyclerView.Adapter<UrlAdapter.UrlViewHolder> {
    
    private List<BlockedUrl> urlList;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onDeleteClick(BlockedUrl blockedUrl);
    }
    
    public UrlAdapter(List<BlockedUrl> urlList, OnItemClickListener listener) {
        this.urlList = urlList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public UrlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blocked_url, parent, false);
        return new UrlViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull UrlViewHolder holder, int position) {
        BlockedUrl blockedUrl = urlList.get(position);
        holder.bind(blockedUrl);
    }
    
    @Override
    public int getItemCount() {
        return urlList.size();
    }
    
    class UrlViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewUrl;
        private TextView textViewDate;
        private TextView textViewStatus;
        private ImageButton buttonDelete;
        
        public UrlViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUrl = itemView.findViewById(R.id.textViewUrl);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
        
        public void bind(BlockedUrl blockedUrl) {
            // Format URL for display
            textViewUrl.setText(formatUrlForDisplay(blockedUrl.getUrl()));
            
            // Format date
            if (blockedUrl.getAddedAt() > 0) {
                Date date = new Date(blockedUrl.getAddedAt());
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                textViewDate.setText("Thêm lúc: " + sdf.format(date));
            } else {
                textViewDate.setText("Thêm lúc: Không rõ");
            }
            
            // Status
            if ("active".equals(blockedUrl.getStatus())) {
                textViewStatus.setText("🛡️ Đang chặn");
                textViewStatus.setTextColor(itemView.getContext().getColor(R.color.success_color));
            } else {
                textViewStatus.setText("⏸️ Tạm dừng");
                textViewStatus.setTextColor(itemView.getContext().getColor(R.color.warning_color));
            }
            
            // Delete button click
            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(blockedUrl);
                }
            });
        }
        
        private String formatUrlForDisplay(String url) {
            try {
                URL parsedUrl = new URL(url);
                String domain = parsedUrl.getHost();
                String path = parsedUrl.getPath();
                
                // Remove www prefix if present
                if (domain.startsWith("www.")) {
                    domain = domain.substring(4);
                }
                
                // Return domain + path (if path is not empty or just "/")
                if (path.length() > 1) {
                    return domain + path;
                } else {
                    return domain;
                }
            } catch (MalformedURLException e) {
                return url.replace("https://", "").replace("http://", "");
            }
        }
    }
}
