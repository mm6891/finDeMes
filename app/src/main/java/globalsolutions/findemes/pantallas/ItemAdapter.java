package globalsolutions.findemes.pantallas;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Created by manuel.molero on 04/02/2015.
 */
import java.util.List;

import globalsolutions.findemes.R;

public class ItemAdapter extends BaseAdapter {

    private Context context;
    private List<ClipData.Item> items;
    private final Integer[] imageId;

    public ItemAdapter(Context context, List<ClipData.Item> items, Integer[] imageID) {
        this.context = context;
        this.items = items;
        this.imageId = imageID;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Object getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        if (convertView == null) {
            // Create a new view into the list.
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item, parent, false);
        }

        // Set data into the view.
        ImageView ivItem = (ImageView) rowView.findViewById(R.id.ivItem);
        TextView tvTitle = (TextView) rowView.findViewById(R.id.tvTitle);


        ClipData.Item item = this.items.get(position);
        tvTitle.setText(item.getText());
        ivItem.setImageResource(imageId[position]);

        return rowView;
    }

}
