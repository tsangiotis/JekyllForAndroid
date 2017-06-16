package app.wt.noolis.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.wt.noolis.R;
import app.wt.noolis.model.Note;
import app.wt.noolis.utils.Tools;

public class ListAdapterNote extends RecyclerView.Adapter<ListAdapterNote.ViewHolder> implements Filterable{

    private Context context;

    private List<Note> original_items = new ArrayList<>();
    private List<Note> filtered_items = new ArrayList<>();
    private ItemFilter mFilter = new ItemFilter();

    private OnItemClickListener onItemClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView content;
        public TextView time;
        public ImageView image;
        public LinearLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            content = (TextView) v.findViewById(R.id.content);
            time = (TextView) v.findViewById(R.id.time);
            image = (ImageView) v.findViewById(R.id.image);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent);
        }

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public ListAdapterNote(Context context, List<Note> items){
        this.context = context;
        original_items = items;
        filtered_items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_note, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Note n = filtered_items.get(position);
        holder.title.setText(n.getTittle());
        holder.time.setText(Tools.stringToDate(n.getLastEdit()));
        holder.content.setText(n.getContent());
        holder.image.setImageResource(Tools.StringToResId(n.getCategory().getIcon(),context));
        ((GradientDrawable) holder.image.getBackground()).setColor(Color.parseColor(n.getCategory().getColor()));

        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onItemClickListener.onItemClick(v, n);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filtered_items.size();
    }


    public interface OnItemClickListener {
        void onItemClick(View view, Note model);
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String query = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();
            final List<Note> list = original_items;
            final List<Note> result_list = new ArrayList<>(list.size());

            for (int i = 0; i < list.size(); i++) {
                String str_title = list.get(i).getTittle();
                String str_content=list.get(i).getContent();
                if (str_title.toLowerCase().contains(query) || str_content.toLowerCase().contains(query)) {
                    result_list.add(list.get(i));
                }
            }

            results.values = result_list;
            results.count = result_list.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filtered_items = (List<Note>) results.values;
            notifyDataSetChanged();
        }

    }
}
