package app.wt.noolis.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.List;

import app.wt.noolis.R;
import app.wt.noolis.model.CategoryIcon;
import app.wt.noolis.utils.Tools;

/**
 * Created by Kodok on 17/06/2016.
 */
public class ListAdapterCategoryIcon extends RecyclerView.Adapter<ListAdapterCategoryIcon.ViewHolder> {

    private List<CategoryIcon> categoryIconList;
    private static RadioButton lastChecked = null;
    private static int lastCheckedPos = 0;
    private boolean checked[];
    private CategoryIcon selectedCategoryIcon;
    private int clickedPos = 0;
    private Context context;

    public ListAdapterCategoryIcon(Context context, List<CategoryIcon> icons) {
        this.context = context;
        categoryIconList = icons;
        checked = new boolean[categoryIconList.size()];
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_category_icon, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final CategoryIcon icon = categoryIconList.get(position);
        holder.vIcon.setImageResource(Tools.StringToResId(icon.getIcon(),context));
        ((GradientDrawable) holder.vIcon.getBackground()).setColor(Color.parseColor(icon.getColor()));
        holder.radioButton.setChecked(icon.isChecked());
        holder.radioButton.setTag(new Integer(position));


        if (position == 0 && categoryIconList.get(0).isChecked() && holder.radioButton.isChecked()) {
            lastChecked = holder.radioButton;
            lastCheckedPos = 0;
        } else if (holder.radioButton.isChecked()) {
            lastChecked = holder.radioButton;
        }

        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton cb = (RadioButton) v;
                clickedPos = ((Integer) cb.getTag()).intValue();

                if (cb.isChecked()) {

                    if (lastChecked != null) {
                        if (clickedPos != lastCheckedPos) {
                            lastChecked.setChecked(false);
                            categoryIconList.get(lastCheckedPos).setChecked(false);
                        }
                    }

                    lastChecked = cb;
                    lastCheckedPos = clickedPos;

                } else {
                    lastChecked = null;
                }

                categoryIconList.get(clickedPos).setChecked(cb.isChecked());
                selectedCategoryIcon = categoryIconList.get(lastCheckedPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryIconList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView vIcon;
        protected RadioButton radioButton;
        protected LinearLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            vIcon = (ImageView) v.findViewById(R.id.image_icon);
            radioButton = (RadioButton) v.findViewById(R.id.radioSelected);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent);
        }


    }

    public CategoryIcon getSelectedCategoryIcon() {
        return selectedCategoryIcon;
    }

    public void setSelectedRadio(String icon) {
        int pos = 0;
        for (int i = 0; i < categoryIconList.size(); i++) {
            if (categoryIconList.get(i).getIcon().equals(icon)) {
                pos = i;
            }
        }
        clickedPos = pos;
        lastCheckedPos = pos;
        categoryIconList.get(pos).setChecked(true);
        selectedCategoryIcon = categoryIconList.get(pos);
    }
}
