package xsoft.com.rucal;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.views.MapView;

/**
 * Created by Usuario on 22/02/2016.
 */
public class CustomInfoWindow extends MarkerInfoWindow {

    public CustomInfoWindow(MapView mapView) {
        super(R.layout.bubble_vote, mapView);
        Button btn = (Button)(mView.findViewById(R.id.bubble_vote_up));
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Button clicked", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override public void onOpen(Object item){
        super.onOpen(item);
        mView.findViewById(R.id.bubble_vote_up).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.bubble_vote_down).setVisibility(View.VISIBLE);
    }

}