package com.example.myapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppMenuAdapter extends RecyclerView.Adapter<AppMenuAdapter.ViewHolder> {

    private final Context context;
    private final List<AppMenuItem> menuItems;

    public AppMenuAdapter(Context context, List<AppMenuItem> menuItems) {
        this.context = context;
        this.menuItems = menuItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppMenuItem item = menuItems.get(position);
        holder.title.setText(item.getTitle());
        holder.subtitle.setText(item.getSubtitle());

        // 클릭 이벤트
        holder.itemView.setOnClickListener(v -> {
            // 클릭된 메뉴에 따라 세부 내용을 업데이트
            // 예: Intent로 새 Activity를 열거나 View를 전환
        });
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, subtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.menu_title);
            subtitle = itemView.findViewById(R.id.menu_subtitle);
        }
    }
}